import SwiftUI
import ComposeApp

private let minAmount = Double(LoanModelsKt.MIN_LOAN_AMOUNT)
private let maxAmount = Double(LoanModelsKt.MAX_LOAN_AMOUNT)
private let periods: [Int] = [7, 14, 21, 28]

struct ContentView: View {
    private let store: LoanStore

    @State private var subscription: Kotlinx_coroutines_coreDisposableHandle?
    @State private var amount: Double = minAmount
    @State private var period: Int = periods.first ?? 7
    @State private var totalRepayment: Int = 0
    @State private var returnDate: String = ""
    @State private var status: String = "Готово"
    @State private var errorMessage: String?
    @State private var isLoading: Bool = false

    init(store: LoanStore) {
        self.store = store
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                GroupBox("Сумма") {
                    VStack(alignment: .leading, spacing: 12) {
                        Slider(
                            value: Binding(
                                get: { amount },
                                set: { updateAmount($0) }
                            ),
                            in: minAmount...maxAmount,
                            step: 500
                        )
                        HStack {
                            Text(format(amount))
                                .font(.title2).bold()
                            Spacer()
                            Text("USD").foregroundStyle(.secondary)
                        }
                        HStack {
                            Text(format(minAmount))
                            Spacer()
                            Text(format(maxAmount))
                        }
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    }
                }

                GroupBox("Срок") {
                    Picker("Срок", selection: Binding(
                        get: { period },
                        set: { updatePeriod($0) }
                    )) {
                        ForEach(periods, id: \.self) { Text("\($0) дн.") }
                    }
                    .pickerStyle(.segmented)
                }

                GroupBox("Расчет") {
                    VStack(alignment: .leading, spacing: 8) {
                        row("Ставка", "15%")
                        row("К возврату", "\(format(totalRepayment)) USD")
                        row("Дата возврата", returnDate)
                        row("Статус", status)
                    }
                }

                if let error = errorMessage {
                    Text(error)
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity)
                        .padding(12)
                        .background(Color.red.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                Spacer()

                Button {
                    submit()
                } label: {
                    if isLoading {
                        ProgressView()
                    } else {
                        Text("Подать заявку").bold()
                    }
                }
                .buttonStyle(.borderedProminent)
                .frame(maxWidth: .infinity)
                .disabled(isLoading)
            }
            .padding()
            .navigationTitle("Калькулятор займа")
        }
        .onAppear { start() }
        .onDisappear { stop() }
    }

    private func row(_ title: String, _ value: String) -> some View {
        HStack {
            Text(title).foregroundStyle(.secondary)
            Spacer()
            Text(value).bold()
        }
    }

    private func start() {
        subscription?.dispose()
        subscription = store.observeState { state in
            apply(state: state)
        }
    }

    private func stop() {
        subscription?.dispose()
        subscription = nil
    }

    private func updateAmount(_ value: Double) {
        store.dispatch(action: LoanActionAmountChanged(amount: Int32(value)))
    }

    private func updatePeriod(_ value: Int) {
        store.dispatch(action: LoanActionPeriodChanged(periodDays: Int32(value)))
    }

    private func submit() {
        store.dispatch(action: LoanActionSubmit())
    }

    private func apply(state: LoanState) {
        amount = Double(state.amount)
        period = Int(state.periodDays)
        totalRepayment = Int(state.totalRepayment)
        returnDate = state.returnDateLabel
        isLoading = state.isLoading
        errorMessage = state.errorMessage
        status = statusText(from: state)
    }

    private func statusText(from state: LoanState) -> String {
        if state.isLoading { return "Отправка..." }
        if let result = state.submissionResult {
            if let success = result as? SubmissionResultSuccess {
                return "Подтверждение \(success.confirmationId)"
            }
            if let error = result as? SubmissionResultError {
                return "Ошибка: \(error.message)"
            }
        }
        return "Готово"
    }
}

private func format(_ value: Double) -> String {
    let formatter = NumberFormatter()
    formatter.numberStyle = .decimal
    formatter.groupingSeparator = ","
    formatter.maximumFractionDigits = 0
    return formatter.string(from: NSNumber(value: value)) ?? "\(Int(value))"
}

private func format(_ value: Int) -> String {
    format(Double(value))
}

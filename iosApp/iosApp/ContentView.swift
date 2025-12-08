import SwiftUI
import ComposeApp

private let minAmount = Double(LoanModelsKt.MIN_LOAN_AMOUNT)
private let maxAmount = Double(LoanModelsKt.MAX_LOAN_AMOUNT)
private let periods: [Int] = [7, 14, 21, 28]

@Observable class LoanViewModel {
    private let store: LoanStore
    private var subscription: Kotlinx_coroutines_coreDisposableHandle?

    var amount: Double = minAmount
    var period: Int = periods.first ?? 7
    var totalRepayment: Int = 0
    var returnDate: String = ""
    var status: String = "Готово"
    var errorMessage: String?
    var isLoading: Bool = false

    init(store: LoanStore) {
        self.store = store
    }

    func start() {
        subscription?.dispose()
        subscription = store.observeState { [weak self] state in
            guard let self else { return }
            apply(state: state)
        }
    }

    func stop() {
        subscription?.dispose()
        subscription = nil
    }

    func updateAmount(_ value: Double) {
        store.dispatch(action: LoanActionAmountChanged(amount: Int32(value)))
    }

    func updatePeriod(_ value: Int) {
        store.dispatch(action: LoanActionPeriodChanged(periodDays: Int32(value)))
    }

    func submit() {
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

struct ContentView: View {
    @State private var viewModel: LoanViewModel

    init(store: LoanStore) {
        _viewModel = State(initialValue: LoanViewModel(store: store))
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                GroupBox("Сумма") {
                    VStack(alignment: .leading, spacing: 12) {
                        Slider(
                            value: Binding(
                                get: { viewModel.amount },
                                set: { viewModel.updateAmount($0) }
                            ),
                            in: minAmount...maxAmount,
                            step: 500
                        )
                        HStack {
                            Text(format(viewModel.amount))
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
                        get: { viewModel.period },
                        set: { viewModel.updatePeriod($0) }
                    )) {
                        ForEach(periods, id: \.self) { Text("\($0) дн.") }
                    }
                    .pickerStyle(.segmented)
                }

                GroupBox("Расчет") {
                    VStack(alignment: .leading, spacing: 8) {
                        row("Ставка", "15%")
                        row("К возврату", "\(format(viewModel.totalRepayment)) USD")
                        row("Дата возврата", viewModel.returnDate)
                        row("Статус", viewModel.status)
                    }
                }

                if let error = viewModel.errorMessage {
                    Text(error)
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity)
                        .padding(12)
                        .background(Color.red.opacity(0.1))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                Spacer()

                Button {
                    viewModel.submit()
                } label: {
                    if viewModel.isLoading {
                        ProgressView()
                    } else {
                        Text("Подать заявку").bold()
                    }
                }
                .buttonStyle(.borderedProminent)
                .frame(maxWidth: .infinity)
                .disabled(viewModel.isLoading)
            }
            .padding()
            .navigationTitle("Калькулятор займа")
        }
        .onAppear { viewModel.start() }
        .onDisappear { viewModel.stop() }
    }

    private func row(_ title: String, _ value: String) -> some View {
        HStack {
            Text(title).foregroundStyle(.secondary)
            Spacer()
            Text(value).bold()
        }
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

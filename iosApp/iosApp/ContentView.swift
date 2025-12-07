import SwiftUI

private let minAmount = 5_000.0
private let maxAmount = 50_000.0
private let periods = [7, 14, 21, 28]

struct ContentView: View {
    @StateObject private var viewModel = LoanViewModel()

    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                GroupBox("Сумма") {
                    VStack(alignment: .leading, spacing: 12) {
                        Slider(value: $viewModel.amount, in: minAmount...maxAmount, step: 500)
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
                    Picker("Срок", selection: $viewModel.period) {
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
                    Task { await viewModel.submit() }
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

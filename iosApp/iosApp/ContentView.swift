import SwiftUI

private let minAmount = 5_000.0
private let maxAmount = 50_000.0
private let periods = [7, 14, 21, 28]

@MainActor
final class LoanViewModel: ObservableObject {
    @Published var amount: Double {
        didSet { persist() }
    }
    @Published var period: Int {
        didSet { persist() }
    }
    @Published var isLoading = false
    @Published var status: String = "Черновик"
    @Published var errorMessage: String?

    private let interestRate = 0.15
    private let storage = UserDefaults.standard

    init() {
        let savedAmount = storage.double(forKey: "loan_amount")
        let savedPeriod = storage.integer(forKey: "loan_period")
        amount = savedAmount == 0 ? 10_000 : savedAmount
        period = periods.contains(savedPeriod) ? savedPeriod : 14
    }

    var totalRepayment: Double { ceil(amount + amount * interestRate) }

    var returnDate: String {
        let date = Calendar.current.date(byAdding: .day, value: period, to: .now) ?? .now
        let formatter = DateFormatter()
        formatter.dateFormat = "dd.MM.yyyy"
        return formatter.string(from: date)
    }

    func submit() async {
        guard amount >= minAmount, amount <= maxAmount, periods.contains(period) else {
            errorMessage = "Введите корректные значения"
            return
        }
        errorMessage = nil
        isLoading = true
        defer { isLoading = false }
        do {
            let url = URL(string: "https://jsonplaceholder.typicode.com/posts")!
            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            let body: [String: Any] = [
                "amount": Int(amount),
                "period": period,
                "totalRepayment": Int(totalRepayment)
            ]
            request.httpBody = try JSONSerialization.data(withJSONObject: body, options: [])
            let (_, response) = try await URLSession.shared.data(for: request)
            if let http = response as? HTTPURLResponse, http.statusCode >= 200 && http.statusCode < 300 {
                status = "Отправлено"
            } else {
                throw URLError(.badServerResponse)
            }
        } catch {
            status = "Ошибка"
            errorMessage = "Не удалось отправить заявку"
        }
    }

    private func persist() {
        storage.set(amount, forKey: "loan_amount")
        storage.set(period, forKey: "loan_period")
    }
}

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
                            .tint(.white)
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

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    private static final String URL =
            "jdbc:mysql://localhost:3306/"
                    + "bank_transfer_management";

    private static final String USER_NAME = "root";

    // Thay bằng mật khẩu MySQL thật
    private static final String PASSWORD = "123456";

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER_NAME,
                PASSWORD
        );
    }

    public static int inputAccountId(
            Scanner scanner,
            String message
    ) {
        while (true) {
            try {
                System.out.print(message);

                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    throw new IllegalArgumentException(
                            "ID tài khoản không được để trống."
                    );
                }

                int accountId = Integer.parseInt(input);

                if (accountId <= 0) {
                    throw new IllegalArgumentException(
                            "ID tài khoản phải lớn hơn 0."
                    );
                }

                return accountId;

            } catch (NumberFormatException e) {
                System.out.println(
                        "Lỗi: ID tài khoản phải là số nguyên."
                );
            } catch (IllegalArgumentException e) {
                System.out.println("Lỗi: " + e.getMessage());
            }

            System.out.println("Vui lòng nhập lại!");
        }
    }

    public static BigDecimal inputAmount(Scanner scanner) {
        while (true) {
            try {
                System.out.print("Nhập số tiền cần chuyển: ");

                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Số tiền không được để trống."
                    );
                }

                BigDecimal amount = new BigDecimal(input);

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(
                            "Số tiền phải lớn hơn 0."
                    );
                }

                // Không cho nhập quá 2 chữ số thập phân
                if (amount.scale() > 2) {
                    throw new IllegalArgumentException(
                            "Số tiền chỉ được có tối đa "
                                    + "2 chữ số thập phân."
                    );
                }

                return amount;

            } catch (NumberFormatException e) {
                System.out.println(
                        "Lỗi: Số tiền không đúng định dạng."
                );
            } catch (IllegalArgumentException e) {
                System.out.println("Lỗi: " + e.getMessage());
            }

            System.out.println("Vui lòng nhập lại!");
        }
    }

    public static void transferFunds(
            int fromAccount,
            int toAccount,
            BigDecimal amount
    ) {
        String sql = "{CALL transfer_funds(?, ?, ?)}";

        try (Connection connection = getConnection()) {

            // Bắt đầu transaction
            connection.setAutoCommit(false);

            try (CallableStatement callableStatement =
                         connection.prepareCall(sql)) {

                callableStatement.setInt(1, fromAccount);
                callableStatement.setInt(2, toAccount);
                callableStatement.setBigDecimal(3, amount);

                callableStatement.execute();

                // Thành công: lưu cả hai thay đổi
                connection.commit();

                System.out.println(
                        "\nChuyển tiền thành công!"
                );

                System.out.println(
                        "Tài khoản gửi: " + fromAccount
                );

                System.out.println(
                        "Tài khoản nhận: " + toAccount
                );

                System.out.println(
                        "Số tiền: " + amount
                );

            } catch (SQLException e) {
                // Có lỗi: hủy toàn bộ thay đổi
                connection.rollback();

                System.out.println(
                        "\nChuyển tiền thất bại!"
                );

                System.out.println(
                        "Đã rollback transaction."
                );

                System.out.println(
                        "Lỗi: " + e.getMessage()
                );
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println(
                    "Không thể kết nối với MySQL!"
            );

            System.out.println(
                    "Lỗi: " + e.getMessage()
            );
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("CHƯƠNG TRÌNH CHUYỂN TIỀN");
        System.out.println("------------------------");

        int fromAccount = inputAccountId(
                scanner,
                "Nhập ID tài khoản gửi: "
        );

        int toAccount;

        while (true) {
            toAccount = inputAccountId(
                    scanner,
                    "Nhập ID tài khoản nhận: "
            );

            if (fromAccount == toAccount) {
                System.out.println(
                        "Tài khoản gửi và nhận "
                                + "không được giống nhau."
                );
            } else {
                break;
            }
        }

        BigDecimal amount = inputAmount(scanner);

        transferFunds(
                fromAccount,
                toAccount,
                amount
        );

        scanner.close();
    }
}
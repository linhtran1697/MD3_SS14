import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    private static final String URL =
            "jdbc:mysql://localhost:3306/student_management";

    private static final String USER_NAME = "root";

    // Thay bằng mật khẩu MySQL thật của mày
    private static final String PASSWORD = "123456";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                URL,
                USER_NAME,
                PASSWORD
        );
    }

    public static void addStudents() {
        Connection connection = null;

        String sql = "{CALL add_students(?, ?)}";

        try {
            connection = getConnection();

            // Tắt tự động lưu dữ liệu
            connection.setAutoCommit(false);

            try (CallableStatement call =
                         connection.prepareCall(sql)) {

                // Sinh viên thứ nhất
                call.setString(1, "Nguyen Van An");
                call.setInt(2, 20);
                call.executeUpdate();

                // Sinh viên thứ hai
                call.setString(1, "Tran Thi Binh");
                call.setInt(2, 21);
                call.executeUpdate();

                // Sinh viên thứ ba
                call.setString(1, "Le Van Cuong");
                call.setInt(2, 22);
                call.executeUpdate();
            }

            // Thành công thì lưu toàn bộ dữ liệu
            connection.commit();
            System.out.println("Thêm 3 sinh viên thành công!");

        } catch (SQLException e) {
            System.out.println("Thêm sinh viên thất bại!");
            System.out.println("Lỗi: " + e.getMessage());

            if (connection != null) {
                try {
                    // Có lỗi thì hủy toàn bộ thao tác thêm
                    connection.rollback();
                    System.out.println("Đã rollback transaction.");
                } catch (SQLException rollbackError) {
                    System.out.println(
                            "Lỗi rollback: "
                                    + rollbackError.getMessage()
                    );
                }
            }

        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeError) {
                    System.out.println(
                            "Lỗi đóng kết nối: "
                                    + closeError.getMessage()
                    );
                }
            }
        }
    }

    public static void main(String[] args) {
        addStudents();
    }
}
package org.midea.mall;
import java.util.Date;
import java.util.Calendar;
public class OrderSystem {
    public static final String DEFAULT_ORDER_PREFIX = "ORD-";
    private static int totalOrderCount = 0;

    private String orderId;     
    private double amount;      
    private Date createTime;    
    private OrderStatus status; 
    public OrderSystem() {
        this.orderId = generateOrderId();
        this.createTime = getCurrentDate();
        this.status = OrderStatus.getDefaultStatus(); 
        totalOrderCount++;
    }

    public double calculateTotal(double freight) {
        return this.amount + freight;
    }

    public static String generateOrderId() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return DEFAULT_ORDER_PREFIX + year + 
               String.format("%02d", month) + 
               String.format("%02d", day) + 
               "-" + (totalOrderCount + 1);
    }

    // 静态函数：获取当前系统时间
    public static Date getCurrentDate() {
        return new Date();
    }

    // 实例函数：拼接订单信息
    public String getOrderInfo() {
        return "订单ID：" + orderId +
                "\n订单金额：¥" + amount +
                "\n创建时间：" + createTime +
                "\n订单状态：" + status.getDescription();
    }

    // Getter/Setter（访问实例变量的函数）
    public String getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}

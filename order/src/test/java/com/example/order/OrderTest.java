package com.example.order;

import com.example.order.order.Order;
import com.example.order.order.OrderItem;
import com.example.order.order.OrderProperties;
import com.example.order.order.OrderService;
import com.example.order.voucher.FixedAmountVoucher;
import com.example.order.voucher.JdbcVoucherRepository;
import com.example.order.voucher.Voucher;
import com.example.order.voucher.VoucherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Test
    @DisplayName("런타임 의존성")
    void order() throws Exception {
        UUID customerId = UUID.randomUUID();
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(UUID.randomUUID(), 100L, 1));
        FixedAmountVoucher fixedAmountVoucher = new FixedAmountVoucher(UUID.randomUUID(), 10L);
        Order order = new Order(UUID.randomUUID(), customerId, orderItems, fixedAmountVoucher);

        Assert.isTrue(order.totalAmount()==90L,
                MessageFormat.format("totalAmount {0} is not 90L",order.totalAmount()));
        assertThat(order.totalAmount()).isEqualTo(90L);
    }

//    @Test
//    @DisplayName("OrderContext")
//    void order2() throws Exception {
//
//        List<OrderItem> orderItems = new ArrayList<>();
//        orderItems.add(new OrderItem(UUID.randomUUID(), 100L, 1));
//
//        UUID customerId = UUID.randomUUID();
//        OrderContext orderContext = new OrderContext();
//        OrderService orderService = orderContext.orderService();
//        Order order = orderService.createOrder(customerId, orderItems);
//
//        Assert.isTrue(order.totalAmount()==100L,
//                MessageFormat.format("totalAmount {0} is not 100L",order.totalAmount()));
//        assertThat(order.totalAmount()).isEqualTo(100L);
//    }

    @Test
    @DisplayName("AnnotationConfigApplicationContext")
    void order3() throws Exception {
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(UUID.randomUUID(), 100L, 1));
        UUID customerId = UUID.randomUUID();

        AnnotationConfigApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(AppConfiguration.class);
        OrderService orderService = applicationContext.getBean(OrderService.class);
        Order order = orderService.createOrder(customerId, orderItems);

        Assert.isTrue(order.totalAmount()==100L,
                MessageFormat.format("totalAmount {0} is not 100L",order.totalAmount()));
        assertThat(order.totalAmount()).isEqualTo(100L);
    }

    @Test
    @DisplayName("ComponentScan")
    void order4() throws Exception {
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(UUID.randomUUID(), 100L, 1));
        UUID customerId = UUID.randomUUID();

        AnnotationConfigApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(AppConfiguration.class);

        //같은 타입 두개의 빈 등록되어 있을 때
        //BeanFactoryAnnotationUtils.qualifiedBeansOfType(applicationContext.getBeanFactory(), VoucherRepository.class, "memory");

        VoucherRepository voucherRepository = applicationContext.getBean(VoucherRepository.class);
        Voucher voucher = voucherRepository.insert(new FixedAmountVoucher(UUID.randomUUID(), 10L));

        OrderService orderService = applicationContext.getBean(OrderService.class);
        Order order = orderService.createOrder(customerId, orderItems, voucher.getVoucherId());

        Assert.isTrue(order.totalAmount()==90L,
                MessageFormat.format("totalAmount {0} is not 90L",order.totalAmount()));
        assertThat(order.totalAmount()).isEqualTo(90L);
    }

    @Test
    @DisplayName("싱글톤")
    void bean() throws Exception {

        AnnotationConfigApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(AppConfiguration.class);

        VoucherRepository voucherRepository1 = applicationContext.getBean(VoucherRepository.class);
        VoucherRepository voucherRepository2 = applicationContext.getBean(VoucherRepository.class);

        assertThat(voucherRepository1).isEqualTo(voucherRepository2);

        applicationContext.close();//컨테이너에 등록된 빈 소멸
    }

    @Test
    @DisplayName("프로퍼티")
    void property() throws Exception {
        AnnotationConfigApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(AppConfiguration.class);

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String version = environment.getProperty("kdt.version");
        Integer minimumOrderAmount = environment.getProperty("kdt.minimum-order-amount",Integer.class);
        List supportVendors = environment.getProperty("kdt.support-vendors",List.class);
        List description = environment.getProperty("kdt.description", List.class);


        assertThat(version).isEqualTo("v1.0.0");
        assertThat(minimumOrderAmount).isEqualTo(1);
        System.out.println(MessageFormat.format("supportVendors->{0}",supportVendors));
        System.out.println(MessageFormat.format("description->{0}",description));
    }

    @Test
    @DisplayName("프로퍼티 주입받아 사용")
    void OrderProperties() throws Exception {
        AnnotationConfigApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(AppConfiguration.class);

        OrderProperties orderProperties = applicationContext.getBean(OrderProperties.class);

        assertThat(orderProperties.getVersion()).isEqualTo("v1.0.0");
        assertThat(orderProperties.getMinimumOrderAmount()).isEqualTo(1);
        System.out.println(MessageFormat.format("supportVendors->{0}",orderProperties.getSupportVendors()));
        System.out.println(MessageFormat.format("description->{0}",orderProperties.getDescription()));
    }

    @Test
    @DisplayName("profile")
    void profile() throws Exception {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(AppConfiguration.class);

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.setActiveProfiles("dev");
        applicationContext.refresh();

        VoucherRepository voucherRepository = applicationContext.getBean(VoucherRepository.class);

        System.out.println(MessageFormat.format("is JdbcVoucherRepository->{0}",voucherRepository instanceof JdbcVoucherRepository));
        System.out.println(MessageFormat.format("is JdbcVoucherRepository->{0}",voucherRepository.getClass().getCanonicalName()));
    }
}
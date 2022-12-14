package gogitek.restaurentorder.service.serviceimp;

import gogitek.restaurentorder.entity.OrderDetail;
import gogitek.restaurentorder.entity.Orders;
import gogitek.restaurentorder.entity.PreOrder;
import gogitek.restaurentorder.entity.PreOrderDetail;
import gogitek.restaurentorder.repository.CartRepo;
import gogitek.restaurentorder.repository.OrderDetailRepo;
import gogitek.restaurentorder.repository.OrdersRepo;
import gogitek.restaurentorder.service.OrderService;
import gogitek.restaurentorder.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class OrderServiceImp implements OrderService {
    private final OrdersRepo ordersRepo;
    private final UserService userService;

    private final CartRepo cartRepo;

    private final OrderDetailRepo orderDetailRepo;


    @Override
    public Orders saveNewOrder(PreOrder preOrder) {
        Orders order = new Orders();
        order.setCreateAt(new Date());
        order.setDiscount(0D);
        order = ordersRepo.save(order);
        preOrder.setOrderId(order.getId());
        cartRepo.save(preOrder);
        Set<PreOrderDetail> preOrderDetails = preOrder.getPreOrderDetails();
        Set<OrderDetail> orderDetails = new HashSet<>();
        Orders finalOrder = order;
        AtomicReference<Double> totalprice = new AtomicReference<>(0D);
        preOrderDetails.stream().forEach(preOrderDetail -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setPrice(preOrderDetail.getProduct().getSalePrice());
            orderDetail.setProduct(preOrderDetail.getProduct());
            orderDetail.setQuantity(preOrderDetail.getQuantity());
            orderDetail.setOrders(finalOrder);
            totalprice.updateAndGet(v -> v + orderDetail.getPrice() * orderDetail.getQuantity());
            orderDetails.add(orderDetail);
        });
        orderDetailRepo.saveAll(orderDetails);
        order.setTotalPrice(totalprice.get());
        ordersRepo.save(order);
        return order;
    }

    @Override
    public Double loadNewPrice(Long id) {
        Orders orders = ordersRepo.getById(id);
        Double total = orders.getOrderDetails().stream().mapToDouble(orderDetail -> orderDetail.getPrice() * orderDetail.getQuantity()).sum();
        if (orders.getDiscount() > 0)
            return total - total*orders.getDiscount()/100;
        return total;
    }


    @Override
    public List<Orders> getListOrderByCurrentUser() {
        return ordersRepo.getAllByUser(userService.getCurrentUser());
    }

    @Override
    public Orders getOrderById(Long id) {
        return ordersRepo.getById(id);
    }

    @Override
    public void updateStatus(Double discount, Long ordersid) {
        Orders defaultOrder = ordersRepo.getById(ordersid);
        defaultOrder.setDiscount(discount);
        ordersRepo.save(defaultOrder);
    }
}

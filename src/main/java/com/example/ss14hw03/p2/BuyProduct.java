package com.example.ss14hw03.p2;

import com.example.ss14hw03.p2.config.HibernateUtils;
import com.example.ss14hw03.p2.model.Order;
import com.example.ss14hw03.p2.model.Product;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
public class BuyProduct {
    public void buyProduct(Long productId, Long customerId, int quantity) {
        Session session = HibernateUtils.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            // Lấy product với Optimistic Lock
            Product product = session.get(Product.class, productId);
            if (product.getStock() < quantity) {
                throw new Exception("Hết hàng");
            }

            // Tạo đơn hàng
            Order order = new Order();
            order.setCustomerId(customerId);
            order.setProductId(productId);
            order.setQuantity(quantity);
            order.setStatus("PAID");
            session.save(order);

            // Trừ kho
            product.setStock(product.getStock() - quantity);
            session.update(product);

            tx.commit();
        } catch (OptimisticLockException e) {
            if (tx != null) tx.rollback();
            System.out.println("Hệ thống đang bận, vui lòng thử lại sau");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.out.println("Lỗi: " + e.getMessage());
        } finally {
            session.close();
        }
    }

}

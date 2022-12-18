package com.study.batch.batch.domain.account;

import com.study.batch.batch.domain.order.Order;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@NoArgsConstructor
@Getter
@ToString
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String orderItem;
    private Integer price;
    private Date orderDate;
    private Date accountDate;

    public Account(Order orders) {
        this.id = orders.getId();
        this.orderItem = orders.getOrderItem();
        this.price = orders.getPrice();
        this.orderDate = orders.getOrderDate();
        this.accountDate = new Date();
    }
}
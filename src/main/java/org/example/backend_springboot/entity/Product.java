package org.example.backend_springboot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String image;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "available")
    private Boolean available = true;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}

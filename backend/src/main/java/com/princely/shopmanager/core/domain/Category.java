package com.princely.shopmanager.core.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import com.princely.shopmanager.shared.domain.ShopAware;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"products", "parent", "children", "shop"})
@EqualsAndHashCode(callSuper = true, exclude = {"products", "parent", "children", "shop"})
public class Category extends BaseEntity implements ShopAware {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Category> children = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();

    @Builder.Default
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "image_url")
    private String imageUrl;

    /**
     * Returns the shop ID this category belongs to.
     * Required by {@link ShopAware} interface for shop-level access control.
     *
     * @return shop ID, or null if shop is not loaded
     */
    @Override
    public String getShopId() {
        return shop != null ? shop.getId() : null;
    }
}
package com.encore.ordering.item.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private int price;
    private int stockQuantity;
    private String imagePath;
    @Builder.Default
    private String delYn="N";

    @CreationTimestamp
    private LocalDateTime createdTime;

    @UpdateTimestamp
    private LocalDateTime updatedTime;

    public void deleteItem(){
        this.delYn = "Y";
    }

    //연산은 서비스 단에서 하고, Entity에는 의미 최소 기능에 충실하자.
    public void updateStockQuantity(int newQuantity){
        this.stockQuantity = newQuantity;
    }

    public void setImagePath(String imagePath){
        this.imagePath = imagePath;
    }
}

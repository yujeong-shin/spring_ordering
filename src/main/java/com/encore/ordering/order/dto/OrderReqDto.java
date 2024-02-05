package com.encore.ordering.order.dto;

import lombok.Data;

import java.util.List;

//@Data
//public class OrderReqDto {
//    private List<Long> itemIds;
//    private List<Integer> counts;
//}

//예시데이터
/*
{
   "itemIds" : [1, 2],
   "counts" : [10, 20]     : 1번 아이템 10개, 2번 아이템 20개
}

 "itemIds" : [1, 2], "counts" : [10, 20, 30] 으로 넘어오는 경우에는 안됨
 */

@Data
public class OrderReqDto {
    private List<OrderReqItemDto> orderReqItemDtos;
    @Data
    public static class OrderReqItemDto{
        private Long itemId;
        private int count;
    }
}

//예시데이터
/*
{
   "orderReqItemDtos" : [
        {"itemId" : 1, "count" : 10},
        {"itemId" : 2, "count" : 20}
   ]
}
 */
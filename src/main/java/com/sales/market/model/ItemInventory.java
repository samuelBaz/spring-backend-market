/**
 * @author: Edson A. Terceros T.
 */

package com.sales.market.model;

import java.math.BigDecimal;

//@Entity
public class ItemInventory extends ModelBase/*<InventoryDto>*/ {

    private Item item;
    private BigDecimal stockQuantity;
    private BigDecimal lowerBoundThreshold;
    private BigDecimal upperBoundThreshold;
    private BigDecimal totalPrice;

}

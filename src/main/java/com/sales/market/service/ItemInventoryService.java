/**
 * @author: Samuel Bazoalto
 */

package com.sales.market.service;

import com.sales.market.model.ItemInventory;
import com.sales.market.model.ItemInventoryEntry;
import com.sales.market.model.MovementType;

import java.math.BigDecimal;

public interface ItemInventoryService extends GenericService<ItemInventory> {

    ItemInventory updateInventory(ItemInventoryEntry itemInventoryEntry, MovementType movementType);
}
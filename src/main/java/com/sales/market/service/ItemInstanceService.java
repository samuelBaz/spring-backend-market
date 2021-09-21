/**
 * @author: Edson A. Terceros T.
 */

package com.sales.market.service;

import com.sales.market.model.Item;
import com.sales.market.model.ItemInstance;
import com.sales.market.model.ItemInstanceStatus;

import java.util.List;

public interface ItemInstanceService extends GenericService<ItemInstance> {
    List<ItemInstance> getItemInstancesByItemIsAndStatusEquals(Item item, ItemInstanceStatus itemInstanceStatus);

    void setStateAll(String itemInstanceSkus, ItemInstanceStatus screwed);

    void saveAllItemBySkus(String itemInstanceSkus, Item item);

    ItemInstance getByIdentifier(String sku);
}

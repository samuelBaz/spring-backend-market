/**
 * @author: Edson A. Terceros T.
 */

package com.sales.market.repository;


import com.sales.market.model.Item;
import com.sales.market.model.ItemInstance;
import com.sales.market.model.ItemInstanceStatus;

import java.util.List;

public interface ItemInstanceRepository extends GenericRepository<ItemInstance> {
    ItemInstance getByIdentifier(String sku);
}

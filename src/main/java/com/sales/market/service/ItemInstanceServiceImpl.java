/**
 * @author: Edson A. Terceros T.
 */

package com.sales.market.service;

import com.sales.market.model.Item;
import com.sales.market.model.ItemInstance;
import com.sales.market.model.ItemInstanceStatus;
import com.sales.market.repository.GenericRepository;
import com.sales.market.repository.ItemInstanceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemInstanceServiceImpl extends GenericServiceImpl<ItemInstance> implements ItemInstanceService {
    private final ItemInstanceRepository repository;
    private final ItemService itemService;

    public ItemInstanceServiceImpl(ItemInstanceRepository repository, ItemService itemService) {
        this.repository = repository;
        this.itemService = itemService;
    }

    @Override
    protected GenericRepository<ItemInstance> getRepository() {
        return repository;
    }

    @Override
    public ItemInstance bunchSave(ItemInstance itemInstance) {
        // here make all objects save other than this resource
        if (itemInstance.getItem() != null) {
            // todo habria que distinguir si permitiremos guardar y  actualizar o ambos mitando el campo id
            itemService.save(itemInstance.getItem());
        }
        return super.bunchSave(itemInstance);
    }

    @Override
    public List<ItemInstance> getItemInstancesByItemIsAndStatusEquals(Item item, ItemInstanceStatus itemInstanceStatus) {
        List<ItemInstance> itemInstances = repository.findAll();
        List<ItemInstance> result = itemInstances.stream().filter(itemF -> itemF.getItem().getId() == item.getId() &&
                itemF.getItemInstanceStatus().equals(itemInstanceStatus) )
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public void setStateAll(String itemInstanceSkus, ItemInstanceStatus status) {
        String[] skus = itemInstanceSkus.split(" ");
        List<ItemInstance> itemInstances = Arrays.stream(skus).map(repository::getByIdentifier)
                .collect(Collectors.toList());
        itemInstances.forEach(itemInstance -> {
            itemInstance.setItemInstanceStatus(status);
            repository.save(itemInstance);
        });
    }

    @Override
    public void saveAllItemBySkus(String itemInstanceSkus, Item item) {
        String[] skus = itemInstanceSkus.split(" ");
        Arrays.stream(skus).forEach(sku -> {
            ItemInstance itemInstance = new ItemInstance();
            itemInstance.setItemInstanceStatus(ItemInstanceStatus.AVAILABLE);
            itemInstance.setIdentifier(sku);
            itemInstance.setItem(item);
            itemInstance.setPrice(55D);
            repository.save(itemInstance);
        });
    }

    @Override
    public ItemInstance getByIdentifier(String sku) {
        return repository.getByIdentifier(sku);
    }
}

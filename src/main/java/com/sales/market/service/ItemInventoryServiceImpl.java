/**
 * @author: Samuel Bazaolto
 */

package com.sales.market.service;

import com.sales.market.dto.MailDto;
import com.sales.market.model.*;
import com.sales.market.repository.ItemInventoryRepository;
import com.sales.market.repository.GenericRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ItemInventoryServiceImpl extends GenericServiceImpl<ItemInventory> implements ItemInventoryService {
    private final ItemInventoryRepository repository;
    private final ItemInstanceService itemInstanceService;
    private final EmailService emailService;

    public ItemInventoryServiceImpl(ItemInventoryRepository repository, ItemInstanceService itemInstanceService, EmailService emailService) {
        this.repository = repository;
        this.itemInstanceService = itemInstanceService;
        this.emailService = emailService;
    }

    @Override
    protected GenericRepository<ItemInventory> getRepository() {
        return repository;
    }

    @Override
    public ItemInventory save(ItemInventory itemInventory) {
        super.save(itemInventory);
        List<ItemInstance> itemInstanceList = itemInstanceService.getItemInstancesByItemIsAndStatusEquals(itemInventory.getItem(),
                ItemInstanceStatus.AVAILABLE);
        itemInventory.setStockQuantity(new BigDecimal(itemInstanceList.size()));
        double totalPrice = itemInstanceList.stream().mapToDouble(ItemInstance::getPrice).sum();
        itemInventory.setTotalPrice(new BigDecimal(totalPrice));
        return repository.save(itemInventory);
    }

    @Override
    public ItemInventory updateInventory(ItemInventoryEntry itemInventoryEntry, MovementType movementType) {
        ItemInventory itemInventory = itemInventoryEntry.getItemInventory();
        BigDecimal quantity = itemInventoryEntry.getQuantity();
        switch (movementType){
            case REMOVED:
                itemInventory.setStockQuantity(itemInventory.getStockQuantity().subtract(quantity));
                itemInstanceService.setStateAll(itemInventoryEntry.getItemInstanceSkus(), ItemInstanceStatus.SCREWED);
                break;
            case SALE:
                itemInventory.setStockQuantity(itemInventory.getStockQuantity().subtract(quantity));
                itemInstanceService.setStateAll(itemInventoryEntry.getItemInstanceSkus(), ItemInstanceStatus.SOLD);
                break;
            case BUY:
                itemInventory.setStockQuantity(itemInventory.getStockQuantity().add(quantity));
                itemInstanceService.saveAllItemBySkus(itemInventoryEntry.getItemInstanceSkus(), itemInventory.getItem());
                break;
        }
        ItemInventory itemInventoryPersist = repository.save(itemInventory);
        this.save(itemInventoryPersist);
        verifyLowerAndUpperBoundThreshold(itemInventoryPersist);
        return itemInventoryPersist;
    }

    private void verifyLowerAndUpperBoundThreshold(ItemInventory itemInventory) {
        BigDecimal quantity = itemInventory.getStockQuantity();
        if(itemInventory.getLowerBoundThreshold().intValue() >= quantity.intValue()){
            emailService.sendMailOnly(RoleType.GROCER, Affair.INVENTARY_LOWER_BOUND);
        }
        if(itemInventory.getUpperBoundThreshold().intValue() < quantity.intValue()){
            emailService.sendMailOnly(RoleType.GROCER, Affair.INVENTARY_UPPER_BOUND);
        }
    }
}

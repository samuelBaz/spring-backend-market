/**
 * @author: Samuel Bazaolto
 */

package com.sales.market.service;

import com.sales.market.model.ItemInventory;
import com.sales.market.model.ItemInventoryEntry;
import com.sales.market.model.MovementType;
import com.sales.market.repository.ItemInventoryEntryRepository;
import com.sales.market.repository.GenericRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemInventoryEntryServiceImpl extends GenericServiceImpl<ItemInventoryEntry> implements ItemInventoryEntryService {
    private final ItemInventoryEntryRepository repository;
    private final ItemInventoryService itemInventoryService;

    public ItemInventoryEntryServiceImpl(ItemInventoryEntryRepository repository, ItemInventoryService itemInventoryService) {
        this.repository = repository;
        this.itemInventoryService = itemInventoryService;
    }

    @Override
    protected GenericRepository<ItemInventoryEntry> getRepository() {
        return repository;
    }

    @Override
    public ItemInventoryEntry save(ItemInventoryEntry itemInventoryEntry) {
        super.save(itemInventoryEntry);
        MovementType movementType = itemInventoryEntry.getMovementType();
        itemInventoryService.updateInventory(itemInventoryEntry, movementType);
        return repository.save(itemInventoryEntry);
    }
}

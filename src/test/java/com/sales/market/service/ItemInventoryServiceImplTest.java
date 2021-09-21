package com.sales.market.service;

import com.sales.market.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.*;

@SpringBootTest
public class ItemInventoryServiceImplTest {
    @Autowired
    private ItemInventoryService itemInventoryService;
    @Autowired
    private ItemInventoryEntryService itemInventoryEntryService;
    @Autowired
    private ItemInstanceService itemInstanceService;

    @Test
    public void givenItemInstancesWhenAnyEntryThenOnlyItemsInstancesAfterPersist(){
        ItemInventory itemInventory = itemInventoryService.findById(1L);
        Item item = itemInventory.getItem();
        List<ItemInstance> itemInstances = itemInstanceService.getItemInstancesByItemIsAndStatusEquals(item, ItemInstanceStatus.AVAILABLE);
        assertEquals(itemInventory.getStockQuantity().intValue(), itemInstances.size());
    }

    @Test
    public  void givenItemInstancesWhenPersistEntryWithRemovedTwoItemInstancesThenItemInventoryUpdated(){
        ItemInventory itemInventory = itemInventoryService.findById(2L);
        ItemInventoryEntry entry = new ItemInventoryEntry();
        entry.setItemInventory(itemInventory);
        entry.setItemInstanceSkus("SKU-VODKA-CODE77721106006150 SKU-VODKA-CODE77721106006151");
        entry.setMovementType(MovementType.REMOVED);
        entry.setQuantity(new BigDecimal(2));
        ItemInventoryEntry entryPersisted = itemInventoryEntryService.save(entry);
        ItemInventory itemInventoryAfterEntry = entryPersisted.getItemInventory();
        assertEquals(3,itemInventoryAfterEntry.getStockQuantity().intValue());
        assertEquals(165, itemInventoryAfterEntry.getTotalPrice().intValue());
        ItemInstance itemInstance = itemInstanceService.getByIdentifier("SKU-VODKA-CODE77721106006150");
        assertEquals(itemInstance.getItemInstanceStatus(),ItemInstanceStatus.SCREWED);
    }

    @Test
    public  void givenItemInstancesWhenPersistEntryWithBuyThreeItemInstancesThenItemInventoryUpdated(){
        ItemInventory itemInventory = itemInventoryService.findById(3L);
        ItemInventoryEntry entry = new ItemInventoryEntry();
        entry.setItemInventory(itemInventory);
        entry.setItemInstanceSkus("SKU-TEQUILA-CODE77721106006155 SKU-TEQUILA-CODE77721106006156 SKU-TEQUILA-CODE77721106006157");
        entry.setMovementType(MovementType.BUY);
        entry.setQuantity(new BigDecimal(3));
        ItemInventoryEntry entryPersisted = itemInventoryEntryService.save(entry);
        ItemInventory itemInventoryAfterEntry = entryPersisted.getItemInventory();
        assertEquals(8,itemInventoryAfterEntry.getStockQuantity().intValue());
        assertEquals(440, itemInventoryAfterEntry.getTotalPrice().intValue());
        ItemInstance itemInstance = itemInstanceService.getByIdentifier("SKU-TEQUILA-CODE77721106006157");
        assertEquals(itemInstance.getItemInstanceStatus(),ItemInstanceStatus.AVAILABLE);
    }

    @Test
    public  void givenItemInstancesWhenPersistEntryWithSaleFourItemInstancesThenItemInventoryUpdatedAndSendEmailGrocer(){
        ItemInventory itemInventory = itemInventoryService.findById(4L);
        ItemInventoryEntry entry = new ItemInventoryEntry();
        entry.setItemInventory(itemInventory);
        entry.setItemInstanceSkus("SKU-RON-CODE77721106006150 SKU-RON-CODE77721106006151 SKU-RON-CODE77721106006152 SKU-RON-CODE77721106006153");
        entry.setMovementType(MovementType.SALE);
        entry.setQuantity(new BigDecimal(4));
        ItemInventoryEntry entryPersisted = itemInventoryEntryService.save(entry);
        ItemInventory itemInventoryAfterEntry = entryPersisted.getItemInventory();
        assertEquals(1,itemInventoryAfterEntry.getStockQuantity().intValue());
        assertEquals(55, itemInventoryAfterEntry.getTotalPrice().intValue());
        ItemInstance itemInstance = itemInstanceService.getByIdentifier("SKU-RON-CODE77721106006152");
        assertEquals(itemInstance.getItemInstanceStatus(),ItemInstanceStatus.SOLD);
    }

    @Test
    public  void givenItemInstancesWhenPersistEntryWithBuyFortyItemInstancesThenItemInventoryUpdatedAndSendEmailGrocer(){
        persistItemInventoryEntry( "SKU-RON-CODE77721106006160 SKU-RON-CODE77721106006161 SKU-RON-CODE77721106006162 SKU-RON-CODE77721106006163 " +
                "SKU-RON-CODE77721106006164 SKU-RON-CODE77721106006165 SKU-RON-CODE77721106006166 SKU-RON-CODE77721106006167");
        persistItemInventoryEntry( "SKU-RON-CODE77721106006168 SKU-RON-CODE77721106006169 SKU-RON-CODE77721106006170 SKU-RON-CODE77721106006171 " +
                "SKU-RON-CODE77721106006172 SKU-RON-CODE77721106006173 SKU-RON-CODE77721106006174 SKU-RON-CODE77721106006175");
        persistItemInventoryEntry( "SKU-RON-CODE77721106006176 SKU-RON-CODE77721106006177 SKU-RON-CODE77721106006178 SKU-RON-CODE77721106006179 " +
                "SKU-RON-CODE77721106006180 SKU-RON-CODE77721106006181 SKU-RON-CODE77721106006182 SKU-RON-CODE77721106006183");
        persistItemInventoryEntry("SKU-RON-CODE77721106006184 SKU-RON-CODE77721106006185 SKU-RON-CODE77721106006186 SKU-RON-CODE77721106006187 " +
                "SKU-RON-CODE77721106006188 SKU-RON-CODE77721106006189 SKU-RON-CODE77721106006190 SKU-RON-CODE77721106006191");
        persistItemInventoryEntry( "SKU-RON-CODE77721106006192 SKU-RON-CODE77721106006193 SKU-RON-CODE77721106006194 SKU-RON-CODE77721106006195 " +
                "SKU-RON-CODE77721106006196 SKU-RON-CODE77721106006197 SKU-RON-CODE77721106006198 SKU-RON-CODE77721106006199");

        ItemInventory itemInventoryAfterEntry = itemInventoryService.findById(4L);
        assertEquals(45,itemInventoryAfterEntry.getStockQuantity().intValue());
        assertEquals(2475, itemInventoryAfterEntry.getTotalPrice().intValue());
        ItemInstance itemInstance = itemInstanceService.getByIdentifier("SKU-RON-CODE77721106006192");
        assertEquals(itemInstance.getItemInstanceStatus(),ItemInstanceStatus.AVAILABLE);
    }

    private void persistItemInventoryEntry(String skus) {
        ItemInventory itemInventory = itemInventoryService.findById(4L);
        ItemInventoryEntry entry = new ItemInventoryEntry();
        entry.setItemInventory(itemInventory);
        entry.setItemInstanceSkus(skus);
        entry.setMovementType(MovementType.BUY);
        entry.setQuantity(new BigDecimal(8));
        itemInventoryEntryService.save(entry);
    }


}
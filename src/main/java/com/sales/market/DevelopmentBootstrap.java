/**
 * @author: Edson A. Terceros T.
 */

package com.sales.market;

import com.sales.market.model.*;
import com.sales.market.repository.BuyRepository;
import com.sales.market.repository.EmployeeRepository;
import com.sales.market.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class DevelopmentBootstrap implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BuyRepository buyRepository;
    private final CategoryService categoryService;
    private final SubCategoryService subCategoryService;
    private final ItemService itemService;
    private final ItemInstanceService itemInstanceService;
    private final EmployeeRepository employeeRepository;
    private final UserService userService;
    private final RoleService roleService;
    private final ItemInventoryService itemInventoryService;

    SubCategory alcoholicSubCat = null;

    public DevelopmentBootstrap(BuyRepository buyRepository, CategoryService categoryService,
                                SubCategoryService subCategoryService, ItemService itemService, ItemInstanceService itemInstanceService,
                                EmployeeRepository employeeRepository, UserService userService, RoleService roleService, ItemInventoryService itemInventoryService) {
        this.buyRepository = buyRepository;
        this.categoryService = categoryService;
        this.subCategoryService = subCategoryService;
        this.itemService = itemService;
        this.itemInstanceService = itemInstanceService;
        this.employeeRepository = employeeRepository;
        this.userService = userService;
        this.roleService = roleService;
        this.itemInventoryService = itemInventoryService;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.logger.info("The seeds will be inserted");
        persistCategoriesAndSubCategories();
        persistItemsAndItemInstances();
        initializeRoles();
        initializeEmployees();
    }

    private void persistItemsAndItemInstances() {
        List<Item> itemsToPersist = persistItems();
        for (Item item : itemsToPersist) {
            persistItemInstances(item);
        }
        persistItemInventory(itemsToPersist);
    }

    private void persistItemInventory(List<Item> itemsToPersist) {
        for (Item item : itemsToPersist) {
            ItemInventory itemInventory = new ItemInventory();
            itemInventory.setItem(item);
            itemInventory.setLowerBoundThreshold(new BigDecimal("1"));
            itemInventory.setUpperBoundThreshold(new BigDecimal("25"));
            itemInventoryService.save(itemInventory);
        }
    }

    private void initializeRoles() {
        createRole(RoleType.ADMIN.getId(), RoleType.ADMIN.getType());
        createRole(RoleType.GENERAL.getId(), RoleType.GENERAL.getType());
        createRole(RoleType.SUPERVISOR.getId(), RoleType.SUPERVISOR.getType());
        createRole(RoleType.GROCER.getId(), RoleType.GROCER.getType());
    }

    private Role createRole(long id, String roleName) {
        Role role = new Role();
        role.setId(id);
        role.setName(roleName);
        roleService.save(role);
        return role;
    }

    private void initializeEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        if (employees.isEmpty()) {
            createEmployee("Alejandro", "Cabrera", "alejandro@gmail.com", false, 1L);
            createEmployee("Edson", "Hidalgo", "edson@gmail.com", false, 2L);
            createEmployee("Ariel", "Lopez", "ariel@gmail.com", false, 3L);
            createEmployee("Gonzalo", "Perez", "bazoaltosamuel33@gmail.com", false, 4L);
            createEmployee("System", "", "system@gmail.com", true, 1L);
        }
    }

    private void createEmployee(String firstName, String lastName, String email, boolean system, long rolId) {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employeeRepository.save(employee);
        createUser(email, employee, system, rolId);
    }

    private void createUser(String email, Employee employee, boolean system, long rolId) {
        User user = new User();
        Role role = new Role();
        HashSet<Role> roles = new HashSet<>();

        user.setEmail(email);
        user.setEnabled(true);
        user.setSystem(system);
        user.setPassword("$2a$10$XURPShQNCsLjp1ESc2laoObo9QZDhxz73hJPaEv7/cBha4pk0AgP.");
        user.setEmployee(employee);

        role.setId(rolId);
        roles.add(role);
        user.setRoles(roles);
        userService.save(user);
    }


    private void persistItemInstances(Item item) {
        for (int i = 0; i < 5; i++) {
            ItemInstance itemInstance = createItemInstance(item, "SKU-"+item.getCode()+"7772110600615"+i, 55D, ItemInstanceStatus.AVAILABLE);
            itemInstanceService.save(itemInstance);
        }
    }

    private ItemInstance createItemInstance(Item item, String sku, double price, ItemInstanceStatus itemInstanceStatus) {
        ItemInstance itemInstance = new ItemInstance();
        itemInstance.setItem(item);
        itemInstance.setFeatured(true);
        itemInstance.setPrice(price);
        itemInstance.setIdentifier(sku);
        itemInstance.setItemInstanceStatus(itemInstanceStatus);
        return itemInstance;
    }

    private List<Item> persistItems(){
        Item whiskyItem = persistItem(alcoholicSubCat, "WHISKY", "WHISKY-CODE");
        Item vodkaItem = persistItem(alcoholicSubCat, "VODKA", "VODKA-CODE");
        Item tequilaItem = persistItem(alcoholicSubCat, "TEQUILA", "TEQUILA-CODE");
        Item ronItem = persistItem(alcoholicSubCat, "RON", "RON-CODE");
        Item [] items = {whiskyItem, vodkaItem, tequilaItem, ronItem};
        return Arrays.asList(items.clone());
    }

    private Item persistItem(SubCategory subCategory, String name, String code) {
        Item item = new Item();
        item.setCode(code);
        item.setName(name);
        item.setSubCategory(subCategory);
        return itemService.save(item);
    }

    private void persistCategoriesAndSubCategories() {
        Category category = persistCategory("BEVERAGE","BEVERAGE-CODE");
        persistCategory("PRESERVES","PRESERVES-CODE");
        persistCategory("APPETIZERS","APPETIZERS-CODE");
        persistSubCategory("SODA", "SODA-CODE", category);
        alcoholicSubCat = persistSubCategory("ALCOHOLIC", "ALCOHOLIC-CODE", category);
        persistSubCategory("JUICE", "JUICE-CODE", category);
    }

    private Category persistCategory(String name, String code) {
        Category category = new Category();
        category.setName(name);
        category.setCode(code);
        return categoryService.save(category);
    }

    private SubCategory persistSubCategory(String name, String code, Category category) {
        SubCategory subCategory = new SubCategory();
        subCategory.setName(name);
        subCategory.setCode(code);
        subCategory.setCategory(category);
        return subCategoryService.save(subCategory);
    }

}

/**
 * @author: Edson A. Terceros T.
 */

package com.sales.market.model;

import java.math.BigDecimal;

//@Entity
public class ItemInventoryEntry extends ModelBase/*<InventoryDto>*/ {

    private ItemInventory itemInventory;
    private MovementType movementType;
    private BigDecimal quantity; // represent sale or buy instances quantity
    private String itemInstanceSkus; //represents a list of the sku of the involved item instances

    /*
    Take into account sku cannot be duplicated
    In the service make possible:
       register buy item instances -> Si no existe el producto crearlo, registrar instancias,
                                        crear y actualizar el ItemInventory correspondiente con sus totalizados
                                        Generar los ItemInventoryEntry para reflejar la operacion de entrada o salida
                                         de almacen

       vender un producto
       desechar un producto similar a una venta pero a costo 0. Debe reflejar el totalizado correctamente de
       ItemInventory

       Debe haber tests unitarios que muestren escenarios para estas operaciones en casos de exito y de error.
    */
}

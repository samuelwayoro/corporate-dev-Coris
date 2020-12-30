/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
Ext.define('MMG.store.Menucheck', {
    extend: 'Ext.data.TreeStore',
    storeId : 'listmenucheck',
    autoLoad: false,
    proxy: {
        type: 'ajax',
        url: 'ExtjsReader?action=checkTree&table=menus'
    },
    sorters: [{
        property: 'leaf',
        direction: 'ASC'
    }, {
        property: 'text',
        direction: 'ASC'
    }]
});


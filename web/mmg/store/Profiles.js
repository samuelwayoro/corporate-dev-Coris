/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
Ext.define('MMG.store.Profiles', {
    extend: 'Ext.data.Store',
    model: 'MMG.model.Profiles',
    storeId : 'listprofiles',
    autoLoad: false,
    pageSize: 10, // nombre de lignes par page
    proxy: {
        type: 'ajax',
        api: {
            read:    'ControlServlet?action=readProfils'
        },
        reader: {
            type: 'json',
            root: 'data',
            successProperty: 'success',
            totalProperty: 'total'
        }
    }
});




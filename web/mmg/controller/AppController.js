/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


// Definition de la classe controller globale
Ext.define('MMG.controller.AppController', {
    extend: 'Ext.app.Controller',
    stores: [
        
        'Profiles'

    ],
    views: [
        'profiles.GridProfiles'
        
    ],
    requires: [
        
        'MMG.util.Util'
    ],
    init: function() {
        console.log('Initialized AppController & Variables=' );

        this.control({
            'viewport > panel': {
                render: this.onPanelRendered
            }

        });
    },
    onPanelRendered: function() {
        console.log('The panel was rendered connected user vars=' );

        console.log('Option saisie des profiles actionn�e!');

        var screenregion = Ext.ComponentQuery.query('#mainContent')[0];
        var panel = this.getView('profiles.GridProfiles').create();
        screenregion.removeAll();
        screenregion.add(panel, true);
        var store = Ext.getCmp("vueprofiles").getStore();
        store.loadData([], false);
        store.setProxy({
            type: 'ajax',
            api: {
                read: 'ControlServlet?action=readProfils'
            },
            reader: {
                type: 'json',
                root: 'data',
                successProperty: 'success',
                totalProperty: 'total'
            }
        });
        store.load(function(records, operation, success) {
            console.log('Store PROFILES has been reloaded');
        });
        //store.sync();
        //store.loadPage(1);
        Ext.getCmp('vueprofiles').getView().refresh();
    }

});
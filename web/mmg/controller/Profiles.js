/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
var thisuser=' ';
var recordid;
var groupmenu;
var pages;
Ext.define('MMG.controller.Profiles', {
    extend: 'Ext.app.Controller',
    requires: ['Ext.window.MessageBox'],
    stores: ['Profiles','Menucheck'],
    views: [
        'profiles.Menucheck',
        'profiles.GridProfiles',
        'profiles.Edit',
        'profiles.Create'
    ],
    
    init: function() {
        console.log('Initialized Profiles! This happens before the Application launch function is called');
        
        this.control({
            'viewport > panel': {
                render: this.onPanelRendered
            },
            'TabProfiles': {
                itemdblclick: this.editProfile,
                selectionchange: this.selectProfile
            },
            'TabProfiles button[action=addProfile]': {
                click: this.newProfile
            },
            
            'TabProfiles button[action=deleteProfile]': {
                click: this.deleteProfileClick
            },
            'profileEdit button[action=save]': {
                click: this.updateProfile
            },
            'profileCreate button[action=save]': {
                click: this.createProfile
            },
            'TabProfiles button[action=frmclose]': {
                click: this.frmExit
            }            
        });
    },
    
    onPanelRendered: function() {
        console.log('In form profile.');
    },
    
    selectProfile: function(selModel, selections) {
        console.log('select profile'); 
        var delBut = Ext.ComponentQuery.query('#btndelprofile')[0];
        delBut.setDisabled(false);
    }, 
    editProfile: function(grid, record) {
        console.log('Double clicked Occured! ID=' + record.get('IDPROFIL')+' CODE='+record.get('NOMPROFIL')+' LIBELLE'+record.get('LIBELLEPROFIL'));
        recordid = record.get('idprofil');
        groupmenu = record.get('nomprofil');
        pages = record.get('REGTACHE');
        var storeMenu = this.getMenucheckStore();
        //var store = Ext.getCmp("vueusers").getStore();
       // storeMenu.load([]);        
        storeMenu.setProxy({
                type: 'ajax',
                url:    'ControlServlet?action=loadTree&table=menus&menugroup='+groupmenu+'&recordid='+recordid
                ,
                sorters: [{
                    property: 'leaf',
                    direction: 'ASC'
                }, {
                    property: 'text',
                    direction: 'ASC'
                }]

            });
            storeMenu.load();
        var view = Ext.widget('profileEdit');
        view.down('form').loadRecord(record);
       
        
    },
    newProfile: function(button) {
        console.log('Create PROFILE');  
        var view = Ext.widget('profileCreate');
        var storeMenu = this.getMenucheckStore();
        //var store = Ext.getCmp("vueusers").getStore();
        //storeMenu.load([]);        
        storeMenu.setProxy({
                type: 'ajax',
                url:    'ControlServlet?action=loadTree&table=menus&menugroup=&recordid=',
                sorters: [{
                    property: 'leaf',
                    direction: 'ASC'
                }, {
                    property: 'text',
                    direction: 'ASC'
                }]

            });
            storeMenu.load();  
        view.down('form');
    },
    updateProfile: function(button) {
        console.log('clicked the Update button');
            var win    = button.up('window'),
            form   = win.down('form'),
            record = form.getRecord(),
            values = form.getValues();
            var formulaire = form.getForm();
            var store = this.getProfilesStore();
            console.log(formulaire);
             //Recuperation des menu selectionnes
            var tree = Ext.getCmp("paneltreemenus");
            var records = tree.getChecked(),
                names = [];var options=';';
            Ext.Array.each(records, function(rec){
                names.push(rec.get('text'));
                if(rec.get('text') !== null){
                    options = options + rec.get('text') +';';
                }
            });
             console.log('Selected menus are:'+options);
        if (formulaire.isValid()) {            
            formulaire.submit({
                    url: 'ControlServlet?action=updateProfils&options='+options,
                    params: {
                       node: recordid
                    },
                    waitMsg: 'Mise a jour...',
                    success: function(form, action){
                        record.set(values);
                        win.close();
                        store.loadPage(1);
                        Ext.getCmp('vueprofiles').getView().refresh();

                    },
                    failure: function(form, action){
                        //win.close();
                        if (action.failureType === Ext.form.Action.CLIENT_INVALID) {
                                Ext.Msg.alert("Ne peut soumettre", "Certains champs sont invalides");
                         } else if (action.failureType === Ext.form.Action.CONNECT_FAILURE) {
                                Ext.Msg.alert('Echec', 'Pb de connection au serveur ');
                         } else if (action.failureType === Ext.form.Action.SERVER_INVALID) {
                                Ext.Msg.alert('Attention', action.result.msg);
                         }
                     }
            });

        }
    },
    createProfile: function(button) {
        console.log('clicked the Save button Create');
            var win    = button.up('window'),
            form   = win.down('form'),
            record = form.getRecord(),
            values = form.getValues();
            var store = this.getProfilesStore();
            //Recuperation des menu selectionnes
            var tree = Ext.getCmp("paneltreemenus");
            var records = tree.getChecked(),
                names = [];var options=';';
            Ext.Array.each(records, function(rec){
                names.push(rec.get('text'));
                if(rec.get('text') !== null){
                    options = options + rec.get('text') +';';
                }
            });
            console.log('Selected menus are:'+options);
            var formulaire = form.getForm();
            if (formulaire.isValid()) {
        form.submit({
                url: 'ControlServlet?action=createProfils&options='+options,
                waitMsg: 'Enregistrement...',
                success: function(form, action){
                    store.add(values);
                    win.close();
                    store.loadPage(1);
                    Ext.getCmp('vueprofiles').getView().refresh();

                },
                failure: function(form, action){
                   // win.close();
                    if (action.failureType === Ext.form.Action.CLIENT_INVALID) {
                            Ext.Msg.alert("Ne peut soumettre", "Certains champs sont invalides");
                     } else if (action.failureType === Ext.form.Action.CONNECT_FAILURE) {
                            Ext.Msg.alert('Echec', 'Pb de connection au serveur ');
                     } else if (action.failureType === Ext.form.Action.SERVER_INVALID) {
                            Ext.Msg.alert('Attention', action.result.errormsg);
                     }
                 }
        });
            }
    },
    
    deleteProfileClick: function(grid, record) {
        console.log('clicked the Delete button');
        var codef;
        var viewList = Ext.ComponentQuery.query('#formprofiles')[0];
        var selection=viewList.getSelectionModel().getSelection()[0];
        if(selection)
        {

            recordid = selection.get('idprofil');
            codef = selection.get('nomprofil');
            console.log('ID='+recordid+' nomprofil'+codef);
            var store = this.getProfilesStore();
            var msglib = 'Etes - vous certain de vouloir supprimer le profil [ '+codef+' ]';
            Ext.MessageBox.confirm('Suppression',msglib , processResult(btn));
            function processResult (btn) {
            if (btn === 'yes'){
                Ext.Ajax.request({
                    url: 'ControlServlet',
                    params: {
                       action: 'deleteProfils',
                       node: recordid
                   },
                    success: function(response){
                        store.remove(selection);
                    },
                    failure: function() {
                            Ext.Msg.show({
                                title:'Suppression',
                                msg: 'La suppression a &eacute;chou&eacute;. Merci de r&eacute;essayer ult&eacute;rieurement.',
                                buttons: Ext.Msg.OK,
                                animEl: 'elId',
                                icon: Ext.MessageBox.ERROR
                            });
                    }
                    });

            }
            return true;
        }
                
    }
    },
    frmExit: function() {
        console.log('Click sur le bouton Fermer du formulaire profiles');
        var screenregion = Ext.ComponentQuery.query('#mainContent')[0];
        var panelobj = Ext.ComponentQuery.query('#formprofiles')[0];
        screenregion.remove(panelobj,true);
    }            
});






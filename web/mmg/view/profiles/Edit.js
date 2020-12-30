/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
Ext.define('MMG.view.profiles.Edit', {
    extend: 'Ext.window.Window',
    alias : 'widget.profileEdit',
    title : 'Modifier Profile',
    //layout: 'fit',
    autoShow: true,
    modal: true,

    initComponent: function() {
        this.items = [
            {
                xtype: 'form',
                id:'frmeditProfiles',
                width: 600,
                frame: true,
                fieldDefaults:{
                    labelAlign: 'left',
                    labelWidth: 90,
                    anchor: '100%'
                },                
                items: [
                    {
                        xtype: 'displayfield',
                        name : 'idprofil',
                        fieldLabel: 'ID'
                    },
                    {
                        xtype: 'textfield',
                        name : 'nomprofil',
                        anchor: '30%',
                        fieldLabel: 'Code Profil'
                    },
                    {
                        xtype: 'textfield',
                        name : 'libelleprofil',
                        fieldLabel: 'Description'
                    }  
                ]
            },{
                xtype: 'treemenus'
                
                
            }
        ];

        this.buttons = [
            {
                text: 'Enregistrer',
                action: 'save'
            },
            {
                text: 'Annuler',
                scope: this,
                handler: this.close
            }
        ];

        this.callParent(arguments);
    }
});




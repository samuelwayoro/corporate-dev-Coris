/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor. 
 */
Ext.define('MMG.view.profiles.Create', {
    extend: 'Ext.window.Window',
    alias : 'widget.profileCreate',
    title : 'Cr&eacute;ation des profils utilisateurs',
    modal: true,
    autoShow: true,

    initComponent: function() {
        this.items = [
            {
                xtype: 'form',
                width: 600,
                frame: true,
                fieldDefaults:{
                    labelAlign: 'left',
                    labelWidth: 90,
                    anchor: '100%'
                },
                items: [
                    
                    {
                        xtype: 'textfield',
                        name : 'nomprofil',
                        anchor: '30%',
                        fieldLabel: 'Code Profil',
                        allowBlank: false
                    },
                    {
                        xtype: 'textfield',
                        name : 'libelleprofil',
                        fieldLabel: 'Description',
                        allowBlank: false
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

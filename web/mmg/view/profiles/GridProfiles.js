/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
Ext.define('MMG.view.profiles.GridProfiles' ,{
    extend: 'Ext.grid.Panel',
    alias : 'widget.TabProfiles',
    id: 'vueprofiles',
    itemId: 'formprofiles',
    store: 'Profiles',
    title : 'Cr&eacute;ation des Profiles',
    width: 900,
    height: 300,
    dockedItems: [
        
    {
        xtype: 'pagingtoolbar',
        store: 'Profiles',   // la source de donn�e de la grille
        dock: 'bottom',
        displayInfo: true
    },        
        {
            xtype: 'toolbar',
            dock: 'top',
            items: [          
                {iconCls: 'enr_add',scale:'medium',text: 'Ajouter', itemId: 'btnaddprofile', action: 'addProfile'},
                '-',
                {iconCls: 'enr_delete',scale:'medium',text: 'Supprimer',disabled: true,itemId: 'btndelprofile',action: 'deleteProfile'},
                '->',
                { iconCls: 'close_form',scale:'medium',text:'Fermer',itemId: 'closeprofile',action: 'frmclose' }
             ]        
        }
    ],
    initComponent: function() {

        this.columns = [{header: 'ID',  dataIndex: 'idprofil',  width: 50},
            {header: 'NOM PROFIL',  dataIndex: 'nomprofil',  width: 70},
            {header: 'LIBELLE',  dataIndex: 'libelleprofil',  width: 230},
            {header: 'REGTACHE',  dataIndex: 'regtache',  flex: 1}
        ];

        this.callParent(arguments);
    }
});





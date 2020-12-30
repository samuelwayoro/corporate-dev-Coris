/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
Ext.define('MMG.view.profiles.Menucheck' ,{
    extend: 'Ext.tree.Panel',
    alias : 'widget.treemenus',
    id: 'paneltreemenus',
    itemId: 'formtreemenus',
    store: 'Menucheck',
    rootVisible: false,
    useArrows: true,
    frame: true,
    title: 'Choix des options du profil',
    width: 600,
    height: 350,
    dockedItems: [{
        xtype: 'toolbar',
        items: {
            text: 'Afficher les selections',
            handler: function(){
                var tree = Ext.getCmp("paneltreemenus");
                var records = tree.getChecked(),
                    names = [];

                Ext.Array.each(records, function(rec){
                    names.push(rec.get('text'));
                });

                Ext.MessageBox.show({
                    title: 'Selected Nodes',
                    msg: names.join('<br />'),
                    icon: Ext.MessageBox.INFO
                });
            }
        }
    }]
});


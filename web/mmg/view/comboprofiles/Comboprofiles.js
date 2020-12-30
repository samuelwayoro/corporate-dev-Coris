/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
Ext.define('MMG.view.comboprofiles.Comboprofiles', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.comboprofiles',
    editable:false,
    queryMode: 'remote',
    valueField: 'CODE',
    displayField: 'CODE',
    store: 'Comboprofiles'
});




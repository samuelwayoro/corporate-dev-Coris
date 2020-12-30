/**
 * Viewport: c'est le container parent de plus haut niveau de toutes
 * les vues.
 *
 * @class MMG.view.Viewport
 * @author Coulibaly Melarga
 Ext.require([
 '*'   
 ]);
 */
// Default menu messagebox
var menuHandler = function(menuOption) {
    var msgtext = 'Probleme de parametrage du menu.Veuillez contacter l\'administrateur';
    Ext.Msg.show({
        title: 'Erreur',
        msg: msgtext,
        buttons: Ext.Msg.OK,
        animEl: 'elId',
        icon: Ext.MessageBox.ERROR
    });
};

var winHeight;
var winWidth;
var tbwidth;
// Variables declaration
winHeight = document.all ? document.body.clientHeight : window.innerHeight;
winWidth = document.all ? document.body.clientWidth : window.innerWidth;
winHeight = winHeight - 60;
tbwidth = winWidth - 5;
var menudata;
var menus;
var menudetail;
var menuparts;
var mnulibelle;
var formulaire;
var toolbar;
var tabmenu;
var smenuExist = 'OK';

// Generation dynamique du menu de l'utilisateur connect�


Ext.define('MMG.view.Viewport', {
    /**
     * Classe parente
     * @property extend
     * @type {String}
     */
    extend: 'Ext.container.Viewport',
    /**
     * D�pendance (un peu l'�quivalence d'un "include" en C++)
     * @property requires
     * @type {Array}
     */
    minHeight: winHeight,
    minWidth: winWidth,
    requires: [
        'Ext.Number',
        'MMG.view.profiles.GridProfiles'
    ],
    // ---
    // --- D�but de d�finition des membres et m�thodes de classes Activations

    /**
     * �quivalent du Java "static"
     */
    statics: {
        // Ici serait d�fini les �l�ments de la classe qui doivent �tre
        // mutualis�s pour toutes les instances de la classe.

        // A priori, nous n'avons rien � d�finir ici...
    },
    // ---
    // --- D�but de d�finition des membres d'instance

    /**
     * Items du viewport, ce sont des vues de plus bas niveau
     * @property items
     * @type {Array}
     */
    items: [
         {
            itemId: 'mainContent',
            region: 'center',
            height: winHeight,
            Width: winWidth,
            //margins: '0 0 0 0',
            //bodyCls: 'foo'
            //bodyStyle: 'margin:0 auto;position:relative;padding:15px;background:#fff;margin:0 10px 0px 10px;',
            layout: {
                type: 'vbox',
                align: 'center',
                pack: 'center'
            }
            

        }

    ],
    // ---
    // --- D�but de d�finition des m�thodes d'instance

    /**
     * M�thode d'initialisation du Viewport.
     * Cette m�thode est un hook, elle sera automatiquement appel�e
     * lors de l'instanciation du viewport.
     * @method initComponent
     */
    initComponent: function() {
        // Le scope en Javascript est �gal � la fonction englobante, on
        // doit donc souvent m�moriser le scope courant. C'est pourquoi, en
        // EXTJS, on utilise souvent cette astuce en d�but de d�finition d'une
        // fonction.
        var me = this;

        // Pour ajouter des membres d'instance de mani�re dynamique � la cr�ation
        // du viewport
        Ext.apply(me, {
            // Exemple inutile: on met une marge al�atoire, on est donc oblig�
            // de faire ceci � l'�x�cution, on ne peut pas faire ceci en dehors
            // de la m�thode initComponent().
            //margin: Ext.Number.randomInt( 1, 20 ) -> desactive le 20/05/2013
            //bodyStyle: 'margin:0 auto;position:relative;min-height:400px;padding:15px;background:#fff;margin:0 10px 0px 10px;'
        });

        // Appel explicite � la m�thode initComponent() de la classe m�re
        // Cet appel est n�cessaire d�s qu'on red�finit initComponent() dans une
        // classe fille (comme c'est ici le cas).
        me.callParent(arguments);
    }
});
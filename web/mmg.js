/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



Ext.application({
    requires: [
        'Ext.container.Viewport'
    ],
    name: 'MMG',
    appFolder: 'mmg',
    controllers: [
        'AppController',
        'Profiles'

    ],
    autoCreateViewport: true,
    launch: function() {
        _App = this;
        Ext.apply(Ext.form.VTypes, {
            /** 
             * The function used to validated multiple email addresses on a single line 
             * @param {String} value The email addresses - separated by a comma or semi-colon 
             */
            'multiemail': function(v) {
                var array = v.split(';');
                var valid = true;
                Ext.each(array, function(value) {
                    if (!this.email(value)) {
                        valid = false;
                        return false;
                    }
                    ;
                }, this);
                return valid;
            },
            /** 
             * The error text to display when the multi email validation function returns false 
             * @type String 
             */
            'multiemailText': '  Ce champ doit &ecirc;tre une adresse e-mail, ou une liste d\'adresses &eacute;lectroniques s&eacute;par&eacute;es par des virgules (;) dans le format  "user@domain.com;test@test.com"',
            /** 
             * The keystroke filter mask to be applied on multi email input 
             * @type RegExp /[a-z0-9_\.\-@\,]/i 
             * /^(([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z]{2,5}){1,25})+([;,.](([a-zA-Z0-9_\-\.]+)@([a-zA-Z0-9_\-\.]+)\.([a-zA-Z]{2,5}){1,25})+)*$/
             */
            'multiemailMask': /[a-z0-9_\.\-@\;]/i
        });
    }
});




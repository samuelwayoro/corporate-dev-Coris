// define the namespaces
jmaki.namespace("jmaki.widgets.yahoo.tooltip");

/**
 * Yahoo jMaki Tooltip Widget
 * 
 * This widget displays static and dynamic tooltips using Our DynamicTooltip 
 * which extends YAHOO.widget.Tooltip
 *
 * Usage: 
 *  1) Load tooltip from test.jsp for elements tt2 and tt4
 *    <a:widget name="yahoo.tooltip" args="{context:['tt2','tt4']}" 
 *          service="test.jsp"/>
 *
 *  2) Load tooltip from text argument for element tt1
 *    <a:widget name="yahoo.tooltip" value="Hello Test"/>
 *
 * @author: Ahmad M. Zawawi (ahmad.zawawi@gmail.com)
 * http://developer.yahoo.com/yui/container/tooltip/index.html
 */
jmaki.widgets.yahoo.tooltip.Widget = function(wargs) {
    var topic = '/yahoo/tooltip';
    var self = this;
    var uuid = wargs.uuid;
    
    //read the widget configuration arguments
    var cfg = {
        context: ['tt1','tt2','tt3'],
        text: '<b>Default Tooltip text</b>',
        showDelay:200,
        hidedelay:250,
        autodismissdelay:5000,
        effect: undefined,
        loadIcon: wargs.widgetDir + "/img/loading.gif",
        loadText: "Loading, please wait...",
        loadError: "Failed to load service!",
        disableCache: false
    };
      
    //read configuration from arguments
    if (typeof wargs.args != 'undefined') {
        
        //the element or elements to watch for on mouseover
        if (typeof wargs.args.context != 'undefined') { 
            cfg.context = wargs.args.context;
        } 
        
        //overide topic name if needed
        if (typeof wargs.args.topic != 'undefined') { 
            topic = wargs.args.topic;
            jmaki.log("Yahoo tooltip: widget uses deprecated topic. Use publish instead.");
        }      
  
        //number of millisecond before showing tooltip
        if (typeof wargs.args.showDelay != 'undefined') { 
            cfg.showDelay = wargs.args.showDelay;
        }      
        //number of millisecond before showing tooltip
        if (typeof wargs.args.hideDelay != 'undefined') { 
            cfg.hideDelay = wargs.args.hideDelay;
        }      
        //number of millisecond before dismissing the tooltip after show
        if (typeof wargs.args.autodismissdelay != 'undefined') { 
            cfg.autodismissdelay = wargs.args.autodismissdelay;
        }      
        //animation effect
        if (typeof wargs.args.effect != 'undefined') { 
            var FADE = {effect:YAHOO.widget.ContainerEffect.FADE,duration:0.5};
            var SLIDE = {effect:YAHOO.widget.ContainerEffect.SLIDE,duration:0.5};
            switch(wargs.args.effect) {
                case 'FADE':
                    cfg.effect = [FADE]     
                    break;
                case 'SLIDE':
                    cfg.effect = [SLIDE];
                    break;
                case 'BOTH':
                    cfg.effect = [FADE,SLIDE];
                    break;
                case 'NONE':
                default:
                    //none is default
                    cfg.effect = undefined;
            }
        }   
        //wait icon
        if (typeof wargs.args.loadIcon != 'undefined') { 
            cfg.loadIcon = wargs.args.loadIcon;
        }
        //wait html text
        if (typeof wargs.args.loadText != 'undefined') { 
            cfg.loadText = wargs.args.loadText;
        }
        //disable dynamic tooltip cache [default:false] 
        if (typeof wargs.args.disableCache != 'undefined') { 
            cfg.disableCache = wargs.args.disableCache;
        }
        //load error html text 
        if (typeof wargs.args.loadError != 'undefined') { 
            cfg.loadError = wargs.args.loadError;
        }
    }

    if (wargs.publish) { 
        topic = wargs.publish 
    }; 

    //Tooltip html text
    if (typeof wargs.value != 'undefined') {
       cfg.text = wargs.value;
    } else if (typeof wargs.service != 'undefined') { 
       cfg.url = wargs.service;
    } 
    
    YAHOO.namespace("widget");
    
    /**
     * Dynamic Tooltip subclass (jMaki implementation)
     * @author Ahmad M. Zawawi (ahmad.zawawi@gmail.com)
     */
    YAHOO.widget.DynamicTooltip = function(uuid,cfg) {
        this.cache = undefined;
        
        //we expose these later...
        this.loadIcon = cfg.loadIcon;
        this.loadText = cfg.loadText;
        this.disableCache = cfg.disableCache;
        this.loadError = cfg.loadError;
        this.url = cfg.url;
        
        //call parent constructor
        YAHOO.widget.DynamicTooltip.superclass.constructor.call(this, uuid, cfg);
    };
    YAHOO.lang.extend(YAHOO.widget.DynamicTooltip, YAHOO.widget.Tooltip);
    
    /**
     * Tooltip.doShow overriden method
     */
    YAHOO.widget.DynamicTooltip.prototype.doShow = function(e, context) {
        var me = this;
        if(typeof me.url == 'undefined') {
            YAHOO.widget.DynamicTooltip.superclass.doShow.call(this, e, context);
            return;
        }

        var yOffset = 25;
        if (this.browser == "opera" && context.tagName && context.tagName.toUpperCase() == "A") {
            yOffset += 12;
        }

        //show after showdelay millisecond(s)
        return setTimeout(
        function() {
            var fetch = (typeof me.cache == 'undefined') || me.disableCache;
            if(fetch) {
                me.setBody("<div style='text-align: center;'>" + me.loadText + 
                "<br/><img src='" + me.loadIcon + "' alt='[loadIcon]'/></div>");
            } else {
                me.setBody(me.cache);
            }
            
            me.moveTo(me.pageX, me.pageY + yOffset);
            
            if (me.cfg.getProperty("preventoverlap")) {
                me.preventOverlap(me.pageX, me.pageY);
            }
            
            YAHOO.util.Event.removeListener(context, "mousemove", me.onContextMouseMove);
            
            me.show();
            
            if(fetch) {
                //connect in the background to bring the url if not cached
                this.dataConn = YAHOO.util.Connect.asyncRequest(
                "GET",
                me.url, {
                    success: function(o) {
                        var body = o.responseText;
                        if(body) {
                            me.cache = body;
                            me.setBody(body);
                            me.moveTo(me.pageX, me.pageY + yOffset);
                        }
                        me.hideProcId = me.doHide();
                    },
                    failure: function(o) {
                        YAHOO.log("failed to load dynamic content");
                        me.setBody(me.loadError);
                        me.moveTo(me.pageX, me.pageY + yOffset);
                        me.hideProcId = me.doHide();
                    }
                });
            }
        },
        this.cfg.getProperty("showdelay"));        
    }
    
    //create our dynamic tooltip and that's it ;-)
    this.tooltip = new YAHOO.widget.DynamicTooltip(uuid,cfg); 
    
} //end of widget
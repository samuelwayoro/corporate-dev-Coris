function removeCharacter(v, ch)
{
    var tempValue = v + "";
    var becontinue = true;
    while (becontinue == true)
    {
        var point = tempValue.indexOf(ch);
        if (point >= 0)
        {
            var myLen = tempValue.length;
            tempValue = tempValue.substr(0, point) + tempValue.substr(point + 1, myLen);
            becontinue = true;
        } else {
            becontinue = false;
        }
    }
    return tempValue;
}

/*
 *  using by function ThausandSeperator!!
 */
function characterControl(value)
{
    var tempValue = "";
    var len = value.length;
    for (i = 0; i < len; i++)
    {
        var chr = value.substr(i, 1);
        if ((chr < '0' || chr > '9') && chr != ' ')
        {
            chr = '';
        }

        tempValue = tempValue + chr;
    }
    return tempValue;
}

/*
 * Automaticly converts the value in the textbox in a currency format with
 * thousands seperator and decimal point
 *
 * @param value : the input text
 * @param digit : decimal number after comma
 */
function ThausandSeperator(value, digit)
{
    var thausandSepCh = " ";
//    var decimalSepCh = "p";

    var tempValue = "";
    var realValue = value + "";
    var devValue = "";
    realValue = characterControl(realValue);
//    var comma = realValue.indexOf(decimalSepCh);
//    if (comma != -1)
//    {
//        tempValue = realValue.substr(0, comma);
//        devValue = realValue.substr(comma);
//        devValue = removeCharacter(devValue, thausandSepCh);
//        devValue = removeCharacter(devValue, decimalSepCh);
//        devValue = decimalSepCh + devValue;
//        if (devValue.length > 3)
//        {
//            devValue = devValue.substr(0, 3);
//        }
//    } else {
//        tempValue = realValue;
//    }
    tempValue = realValue;
    tempValue = removeCharacter(tempValue, thausandSepCh);

    var result = "";
    var len = tempValue.length;
    while (len > 3) {
        result = thausandSepCh + tempValue.substr(len - 3, 3) + result;
        len -= 3;
    }
    result = tempValue.substr(0, len) + result;
    return result + devValue;
}




function formatNumberInInput(input) {

    if (event.which && (event.keyCode != 8) && (event.which < 48 || event.which > 57)) {
        event.preventDefault();
        return false;
    }
    //on sauve la valeur en cours
    var startValue = input.value;

    /* on nettoie le nombre qui pour ne garder que 1111.11
     Dans cette fonction on récupère tous les nombres et . ou , via une expression régulière
     */

    var numbers = startValue.match(/([\d])/g);
    // On ne fait le traitement que si *on a récupéré des nombres, sinon on vide le champ */ //^0
    if (numbers) {
        // numbers est un tableau ( ["1", "2", ".", "4", "5"] ) , on concatene l'ensemble pour n'avoir qu'une seule chaine */
        var cleanNumber = numbers.join("");
        /*   Ensuite on utilise une expression régulière avancée qui s'occupe de récupérer tous les chiffres qui se trouvent avant un groupe de 3 chiffre répété plusieurs fois
         Et pour chacun de ces chiffres ont rajoute un espace après (le $1 représente le chiffre récupéré) */
        //on supprime au passage les nombres au dela de 2 apres le . ou la virgule
        cleanNumber = cleanNumber.replace(/([.,]\d{2})\d+$/, '$1');
        var formattedNumber = cleanNumber.replace(/(\d)(?=(\d{3})+(?:[.,]\d{2})*$)/g, '$1 ');
        //on reinjecte le nouveau nombre dans le input
        if (startValue != formattedNumber) {
            input.value = formattedNumber;
            alert("Valeur de inpur"+input);
        }
    } else {
        input.value = "";
         alert("Valeur de inpur"+input);
    }
}
function currencyFormat(fld, milSep, event) {
    //var key = '';
    var i = j = 0;
    var len = len2 = 0;
    var strCheck = '0123456789';
    var aux = aux2 = '';

//    var keyCode = evt.which ? evt.which : evt.keyCode;
//
//    if (keyCode == 13 || keyCode ==10 || keyCode ==9 ) return true;  // Enter
//    if(keyCode > 31 && (keyCode < 45 || keyCode > 57)) return false;
//    
    if (event.which && (event.keyCode != 8) && (event.which < 48 || event.which > 57)) {
        event.preventDefault();
        return false;
    }

    len = fld.value.length;

    aux = '';
    for (; i < len; i++)
        if (strCheck.indexOf(fld.value.charAt(i)) != -1)
            aux += fld.value.charAt(i);

    len = aux.length;

    if (len > 2) {
        aux2 = '';
        for (j = 0, i = len; i >= 0; i--) {
            if (j == 3) {
                aux2 += milSep;
                j = 0;
            }
            aux2 += aux.charAt(i);
            j++;
        }
        fld.value = '';
        len2 = aux2.length;
        for (i = len2 - 1; i >= 0; i--)
            fld.value += aux2.charAt(i);
    }
    return true;
}
function chiffres(event) {

    if (event.which && (event.keyCode != 8) && (event.which < 48 || event.which > 57)) {
        event.preventDefault();
    }

}
function AddThousandSeparator(str, thousandSeparator, decimalSeparator) {
    var sRegExp = new RegExp('(-?[0-9]+)([0-9]{3})'),
            sValue = str + "", // to be sure we are dealing with a string
            arrNum = [];

    if (thousandSeparator === undefined) {
        thousandSeparator = ",";
    }
    if (decimalSeparator === undefined) {
        decimalSeparator = ".";
    }

    arrNum = sValue.split(decimalSeparator);
// let's be focused first only on the integer part
    sValue = arrNum[0];

    while (sRegExp.test(sValue)) {
        sValue = sValue.replace(sRegExp, '$1' + thousandSeparator + '$2');
    }

// time to add back the decimal part
    if (arrNum.length > 1) {
        sValue = sValue + decimalSeparator + arrNum[1];
    }
    return sValue;
}
function getInputText(text)
{
    alert(text.value);
}

function FormatNumberBy3(num, decpoint, sep) {
    // check for missing parameters and use defaults if so
    if (arguments.length == 2) {
        sep = ",";
    }
    if (arguments.length == 1) {
        sep = ",";
        decpoint = ".";
    }
    // need a string for operations
    num = num.toString();
    // separate the whole number and the fraction if possible
    a = num.split(decpoint);
    x = a[0]; // decimal
    y = a[1]; // fraction
    z = "";


    if (typeof(x) != "undefined") {
        // reverse the digits. regexp works from left to right.
        for (i = x.length - 1; i >= 0; i--)
            z += x.charAt(i);
        // add seperators. but undo the trailing one, if there
        z = z.replace(/(\d{3})/g, "$1" + sep);
        if (z.slice(-sep.length) == sep)
            z = z.slice(0, -sep.length);
        x = "";
        // reverse again to get back the number
        for (i = z.length - 1; i >= 0; i--)
            x += z.charAt(i);
        // add the fraction back in, if it was there
        if (typeof(y) != "undefined" && y.length > 0)
            x += decpoint + y;
    }
    return x;
}

function handleDialogSubmit(xhr, status, args) {
    if (args.validationFailed) {
        confirmation.hide();
    } else {
        confirmation.show();
    }
}
// thousand separates a digit-only string using commas
// by element:  onkeyup = "ThousandSeparate(this)"
// by ID:       onkeyup = "ThousandSeparate('txt1','lbl1')"

function ThousandSeparate()
{
    if (event.which && (event.keyCode != 8) && (event.which < 48 || event.which > 57)) {
        event.preventDefault();
        return false;
    }
    if (arguments.length == 1)
    {
        var V = arguments[0].value;

        V = V.replace(/,/g, '');
        var R = new RegExp('(-?[0-9]+)([0-9]{3})');
        while (R.test(V))
        {
            V = V.replace(R, '$1,$2');
        }
        arguments[0].value = V;
    }
    else if (arguments.length == 2)
    {
        var V = document.getElementById(arguments[0]).value;
        var R = new RegExp('(-?[0-9]+)([0-9]{3})');
        while (R.test(V))
        {
            V = V.replace(R, '$1,$2');
        }
        document.getElementById(arguments[1]).innerHTML = V;
    }
    else
        return false;
}  

 function inhiberInput (idNonInhibe,idInhibe) {
    var dt=document.getElementById(idNonInhibe)
    if (this.dt.value.length > 0) {
        document.getElementById(idInhibe).disabled=true;
 
    }else {
        document.getElementById(idNonInhibe).disabled = false;
         
    }
}


var exec = cordova.require('cordova/exec');

var FitnessPlugin = function() {
    console.log('FitnessPlugin instanced');
};

FitnessPlugin.prototype.show = function(msg, onSuccess, onError) {
    var errorCallback = function(obj) {
        onError(obj);
    };

    var successCallback = function(obj) {
        onSuccess(obj);
    };

    exec(successCallback, errorCallback, 'FitnessPlugin', 'show', [msg]);
};

if (typeof module != 'undefined' && module.exports) {
    module.exports = FitnessPlugin;
}
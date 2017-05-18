define([], function () {
    "use strict";

    const DESC = {
        "$namespace" : "http://plugins.linkedpipes.com/ontology/e-sparqlEndpointChunked#",
        "$type": "Configuration",
        "$options": {
            "$predicate": "auto",
            "$control": "auto"
        },
        "query" : {
            "$type" : "str",
            "$label" : "Endpoint URL"
        },
        "endpoint" : {
            "$type" : "str",
            "$label" : "SPARQL CONSTRUCT query"
        },
        "defaultGraph" : {
            "$type": "value",
            "$array": true,
            "$label" : "Default graph"
        },
        "headerAccept" : {
            "$type" : "str",
            "$label" : "Used MimeType"
        },
        "chunkSize" : {
            "$type" : "int",
            "$label" : "Chunk size"
        }
    };

    function controller($scope, $service) {

        if ($scope.dialog === undefined) {
            $scope.dialog = {};
        }

        DESC.defaultGraph["$onSave"] = $service.v1.fnc.removeEmptyIri;

        const dialogManager = $service.v1.manager(DESC, $scope.dialog);

        $service.onStore = function () {
            dialogManager.save();
        };

        dialogManager.load();

    }

    controller.$inject = ['$scope', '$service'];
    return controller;
});

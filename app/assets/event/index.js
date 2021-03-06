/*
 * Happy Melly Teller
 * Copyright (C) 2013 - 2014, Happy Melly http://www.happymelly.com
 *
 * This file is part of the Happy Melly Teller.
 *
 * Happy Melly Teller is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Happy Melly Teller is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Happy Melly Teller.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you have questions concerning this license or the applicable additional terms, you may contact
 * by email Sergey Kotlov, sergey.kotlov@happymelly.com or
 * in writing Happy Melly One, Handelsplein 37, Rotterdam, The Netherlands, 3071 PR
 */

/**
 * Show success message
 * @param message {string}
 */
function showSuccess(message) {
    $('.alert-block').append(
        $('<div class="alert alert-success">')
            .text(message)
            .append('<button type="button" class="close" data-dismiss="alert">&times;</button>')
    );
}

/**
 * This function collects data from all filters and provides a requests based on their values
 * @returns {string}
 */
function makeRequestUrl() {
    var request = 'events/filtered?';
    var filter = $('#past-future').find(':selected').val();
    var counter = 0;
    if (filter != 'all') {
        request += 'future=' + ((filter == 'past') ? 'false' : 'true');
        counter += 1;
    }
    filter = $('#private').find(':selected').val();
    if (filter != 'all') {
        request += ((counter > 0) ? '&' : '') + 'public=' + ((filter == 'private') ? 'false' : 'true');
        counter += 1;
    }
    filter = $('#archived').find(':selected').val();
    if (filter != 'all') {
        request += ((counter > 0) ? '&' : '') + 'archived=' + ((filter == 'archived') ? 'true' : 'false');
        counter += 1;
    }
    filter = $('#activeBrandId').val();
    request += ((counter > 0) ? '&' : '') + 'brandId=' + filter;
    counter += 1;

    filter = $('#facilitators').find(':selected').val();
    if (filter == undefined) {
        request += ((counter > 0) ? '&' : '') + 'facilitator=' + $('#activeUserId').val();
    } else {
        if (filter != 'all') {
            request += ((counter > 0) ? '&' : '') + 'facilitator=' + filter;
        }
    }
    return request;
}

/**
 * Render a dropdown with actions for an event
 *
 * @param data {object}
 * @returns {string}
 */
function renderDropdown(data) {
    var emptyDropdown = true;
    var html = '<div class="dropdown">';
    html += '<a class="dropdown-toggle" data-toggle="dropdown" href="#"><i class="glyphicon glyphicon-tasks"></i></a>';
    html += '<ul class="dropdown-menu pull-right" aria-labelledby="dLabel">';
    if ('edit' in data && data.edit) {
        if ('edit' in data && data.edit) {
            emptyDropdown = false;
            html += '<li><a tabindex="-1" href="' + data.edit;
            html += '" title="Edit Event"><i class="glyphicon glyphicon-pencil"></i> Edit</a></li>';
        }
        if ('duplicate' in data && data.duplicate) {
            emptyDropdown = false;
            html += '<li><a tabindex="-1" href="' + data.duplicate;
            html += '" title="Duplicate Event"><i class="glyphicon glyphicon-edit"></i> Duplicate</a></li>';
        }
        if ('cancel' in data && data.cancel) {
            emptyDropdown = false;
            html += '<li><a class="delete" tabindex="-1" href="#cancelDialog" data-href="' + data.cancel;
            html += '" data-toggle="modal" title="Cancel Event"><i class="glyphicon glyphicon-trash"></i> Cancel</a></li>';
        }
    }
    html += '</ul></div>';
    if (emptyDropdown) {
        return '';
    }
    return html;
}

$(document).ready( function() {

    var events = $('#events').dataTable({
        "sDom": '<"toolbar">rtip',
        "iDisplayLength": 25,
        "asStripeClasses":[],
        "aaSorting": [],
        "bLengthChange": false,
        "ajax": {
            "url" : makeRequestUrl(),
            "dataSrc": "",
            "deferRender": true
        },
        "order": [[ 3, "asc" ]],
        "columns": [
            { "data": "event" },
            { "data": "facilitators" },
            { "data": "location" },
            { "data": "schedule" },
            { "data": "totalHours" },
            { "data": "invoice" },
            { "data": "confirmed" },
            { "data": "actions" }
        ],
        "columnDefs": [{
            "render": function(data) {
                return '<a href="' + data.url + '">' + data.title + '</a>';
            },
            "targets": 0
        }, {
            "render": function(data) {
                var html = '';
                for (var i = 0; i < data.length; i++) {
                    html += '<div><a href="' + data[i].url + '">' + data[i].name + '</a><br/></div>';
                }
                return html;
            },
            "targets": 1
        },{
            "render": function(data) {
                if (data.online) {
                    return data.city;
                } else {
                    return '<img align="absmiddle" width="16" src="/assets/images/flags/16/' +
                        data.country + '.png"/> ' + data.city;
                }
            },
            "targets": 2
        }, {
            "render": function(data) {
                return data.start + ' / ' + data.end;
            },
            "targets": 3
        }, {
            "render": function(data) {
                if(data.free) {
                    return '<span class="label label-success">free</span>';
                } else {
                    return data.invoice;
                }
            },
            "targets": 5
        }, {
            "render": function(data) {
                if(data) {
                    return '<span class="label label-success">yes</span>';
                } else {
                    return '<span class="label label-danger">no</span>';
                }
            },
            "targets": 6
        },{
            "render": function(data) {
                return renderDropdown(data);
            },
            "targets": 7,
            "orderable": false
        }]
    });
    $("body").css("cursor", "progress");
    events.on( 'xhr.dt', function () {
        $("body").css("cursor", "default");
    });

    $("div.toolbar").html($('#filter-containter').html());
    $('#filter-containter').empty();

    function updateTable() {
        $("body").css("cursor", "progress");
        events
            .api()
            .ajax
            .url(makeRequestUrl())
            .load(function(){
                $("body").css("cursor", "default");
            });
    }
    $('#past-future').on('change', function() { updateTable(); });
    $('#private').on('change', function() { updateTable(); });
    $('#archived').on('change', function() {  updateTable(); });
    $('#facilitators').on('change', function() { updateTable(); });

    events.fnDraw();

    $("#events").on('click', '.delete', function(){
        $("#cancelLink").attr('href', $(this).data('href'));
    });
    $('#cancelLink').on('click', function(e) {
        e.preventDefault();
        $.ajax({
            type: "POST",
            url: $(this).attr("href"),
            data: $("#cancelForm").serialize()
        }).done(function(data){
            updateTable();
            $("#cancelDialog").modal('hide');
            showSuccess("You successfully cancelled the event")
        });
    });
});

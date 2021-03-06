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
 * Changes UI on brand activation/deactivation
 *
 * @param active {boolean} if true, brand is active
 */
function switchState(active) {
    if (active) {
        $('#activate').removeClass('btn-success').addClass('btn-warning');
        $('#activate').html('<i class="glyphicon-off glyphicon glyphicon-white"></i> Deactivate');
        $('#deactivatedStatus').hide();
    } else {
        $('#activate').addClass('btn-success').removeClass('btn-warning');
        $('#activate').html('<i class="glyphicon-off glyphicon glyphicon-white"></i> Activate');
        $('#deactivatedStatus').show();
    }
}

/**
 * Show notification message
 * @param target jQuery identifier of html element
 * @param message {string}
 * @param type {string} 'success/danger'
 */
function notify(target, message, type) {
    $(target).html("");
    $(target).append(
        $('<div class="alert alert-' + type + '">')
            .text(message)
            .append('<button type="button" class="close" data-dismiss="alert">&times;</button>')
    );
}

function showEventTypeNotification(message, type) {
    notify('#notification', message, type)
}

function showBrandLinkNotification(message, type) {
    notify('#brandLinkNotification', message, type)
}

function showTeamNotification(message, type) {
    notify('#teamNotification', message, type);
}

/**
 * Returns data to update event type retrieved from a event type row
 *
 * @param element {object} jQuery table row representation
 */
function collectEventTypePostData(element) {
    var data = [];
    data[0] = { name: 'brandId', value: $(element).data('brandid') };
    var i = 1;
    $(element).children('td').each(function() {
        data[i] = { name: $(this).data('name'), value: $(this).text() };
        i += 1;
    });
    var free = $(element).find('[type="checkbox"]').prop('checked');
    data[i] = { name: 'free', value: free };
    return data;
}

/**
 * Updates event type
 *
 * @param object Changed field
 * @param value New parameter value
 */
function updateEventType(object, value) {
    $('#notification').html("");
    var id = $(object).parent().data('id');
    var brandId = $(object).parent().data('brandid');
    var data = collectEventTypePostData($(object).parent());
    $("body").css("cursor", "progress");
    $.ajax({
        type: "POST",
        url: jsRoutes.controllers.EventTypes.update(brandId, id).url,
        data: data
    }).done(function(data) {
        showEventTypeNotification("You successfully updated the event type", 'success');
        if (value != null) {
            $(object).text(value);
        }
    }).fail(function(jqXHR, status, error) {
        if (status == "error") {
            var error = JSON.parse(jqXHR.responseText);
            showEventTypeNotification(error.message, 'danger');
        } else {
            var msg = "Unexpected error. Please contact the support team.";
            showEventTypeNotification(msg, 'danger');
        }
    }).complete(function() {
        $("body").css("cursor", "default");
    });
    return true;
}

function initializeEventTypesActions() {
    $('#eventTypes').editableTableWidget();
    $('#eventTypes td').on('validate', function(evt, newValue) {
        if (newValue == "") {
            return false; // mark cell as invalid
        }
    }).on('change', function(evt, newValue) {
        return updateEventType($(this), newValue);
    });
    $('#eventTypes input').on('change', function(e) {
        return updateEventType($(this).parent(), null);
    });
}

/**
 * Updates the view after the person was added as a team member
 * @param personId {int} Person id
 * @param name {string} Person name
 * @param brandId {int} Brand id
 */
function addMember(personId, name, brandId) {
    $('#members').append(
        $('<tr>')
            .attr('data-id', personId)
            .attr('data-brandid', brandId)
            .append($('<td>')
                .append($('<a>')
                    .attr('href', jsRoutes.controllers.People.details(personId).url)
                    .append(name)))
            .append('<td><input type="checkbox" value="event"/></td>')
            .append('<td><input type="checkbox" value="evaluation"/></td>')
            .append('<td><input type="checkbox" value="certificate"/></td>')
            .append($('<td>')
                .append($('<a>')
                    .append('<i class="glyphicon glyphicon-trash"></i>')
                    .addClass('remove')
                    .attr('href', '#')
                    .attr('data-id', personId)
                    .attr('data-name', name)
                    .attr('data-href',
                    jsRoutes.controllers.Brands.removeCoordinator(brandId, personId).url)))
    );
    $('select[name="personId"]').children('option[value=' + personId + ']').remove();
}

/**
 * Updates the view after the person was deleted from the team
 * @param personId {int} Person id
 * @param name {string} Person full name
 */
function removeMember(personId, name) {
    $('tr[data-id="' + personId + '"]').remove();
    var people = [];
    var select = 'select[name="personId"]';
    $(select).children('option').each(function() {
        people.push({ id: $(this).val(), name: $(this).text()});
    });
    people.push({ id: personId, name: name });
    people.sort(function(left, right) { return left.name.localeCompare(right.name); });
    $(select).empty();
    for(var i = 0; i < people.length; i++) {
        var data = people[i];
        $(select).append($('<option>').append(data.name).attr('value', data.id));
    }
}

/**
 * Updates the view after the link was added
 * @param linkId {int} Link id
 * @param brandId {int} Brand id
 * @param linkType {string} Link type
 * @param link {string} Link
 */
function addLink(linkId, brandId, linkType, link) {
    $('#links').append(
        $('<tr>')
            .attr('data-id', linkId)
            .attr('data-brandid', brandId)
            .append($('<td>').append(linkType))
            .append($('<td>')
                .append($('<a>')
                    .attr('href', link)
                    .append(link)))
            .append($('<td>')
                .append($('<a>')
                    .append('<i class="glyphicon glyphicon-trash"></i> Remove')
                    .addClass('remove')
                    .attr('href', '#')
                    .attr('data-id', linkId)
                    .attr('data-href', jsRoutes.controllers.BrandLinks.remove(brandId, linkId).url)))
    );
}

/**
 * Updates the view after the link was deleted
 * @param linkId {int} Link id
 */
function removeLink(linkId) {
    $('tr[data-id="' + linkId + '"]').remove();
}

function initializeLinksActions() {
    $('#addLinkForm').submit(function(e) {
        $.post($(this).attr("action"), $(this).serialize(), null, "json").done(function(data) {
            addLink(data.id, data.brandId, data.linkType, data.link);
        }).fail(function(jqXHR, status, error) {
            if (status == "error") {
                var error = JSON.parse(jqXHR.responseText);
                showBrandLinkNotification(error.message, 'danger');
            } else {
                var msg = "Internal error. Please try again or contant the support team.";
                showBrandLinkNotification(msg, 'danger');
            }
        });
        // Prevent the form from submitting with the default action
        return false;
    });
    $('#links').on('click', 'a.remove', function(e) {
        var linkId = $(this).data('id');
        $.ajax({
            type: "DELETE",
            url: $(this).data('href'),
            dataType: "json"
        }).done(function(data) {
            removeLink(linkId, name);
        }).fail(function(jqXHR, status, error) {
            if (status == "error") {
                var error = JSON.parse(jqXHR.responseText);
                showBrandLinkNotification(error.message, 'danger');
            } else {
                var msg = "Internal error. Please try again or contant the support team.";
                showBrandLinkNotification(msg, 'danger');
            }
        });
        return false;
    });
}

function initializeTeamActions() {
    $('#addMemberForm').submit(function(e) {
        $.post($(this).attr("action"), $(this).serialize(), null, "json").done(function(data) {
            addMember(data.data.personId, data.data.name, data.data.brandId);
            showTeamNotification(data.message, 'success');
        }).fail(function(jqXHR, status, error) {
            if (status == "error") {
                var error = JSON.parse(jqXHR.responseText);
                showTeamNotification(error.message, 'danger');
            } else {
                var msg = "Internal error. Please try again or contant the support team.";
                showTeamNotification(msg, 'danger');
            }
        });
        // Prevent the form from submitting with the default action
        return false;
    });
    $('#members').on('click', 'a.remove', function(e) {
        var personId = $(this).data('id'),
            name = $(this).data('name');
        $.ajax({
            type: "DELETE",
            url: $(this).data('href'),
            dataType: "json"
        }).done(function(data) {
            removeMember(personId, name);
            showTeamNotification(data.message, 'success');
        }).fail(function(jqXHR, status, error) {
            if (status == "error") {
                var error = JSON.parse(jqXHR.responseText);
                showTeamNotification(error.message, 'danger');
            } else {
                var msg = "Internal error. Please try again or contant the support team.";
                showTeamNotification(msg, 'danger');
            }
        });
        return false;
    });
    $('#members').on('change', 'input', function(e) {
        var brandId = $(this).parents('tr').data('brandid');
        var personId = $(this).parents('tr').data('id');
        var type = $(this).val();
        var url = jsRoutes.controllers.Brands.turnNotificationOff(brandId, personId, type).url;
        if (this.checked) {
            url = jsRoutes.controllers.Brands.turnNotificationOn(brandId, personId, type).url;
        }
        var that = $(this);
        $.post(url, {}, null, "json").done(function(data) {
            showTeamNotification(data.message, 'success');
        }).fail(function(jqXHR, status, error) {
            if (status == "error") {
                var error = JSON.parse(jqXHR.responseText);
                showTeamNotification(error.message, 'danger');
            } else {
                var msg = "Internal error. Please try again or contant the support team.";
                showTeamNotification(msg, 'danger');
            }
            that.attr('checked', false);
        });
    });
    $('#members .glyphicon-info-sign').tooltip();
}

function initializeActions() {
    initializeEventTypesActions();
    initializeTeamActions();
    initializeLinksActions();
}

/**
 * Loads tab content (if needed) and shows it to a user
 * @param elem Tab button
 * @returns {boolean}
 */
function showTab(elem) {
    var url = $(elem).attr('data-href'),
        target = $(elem).attr('href');
    if ($.inArray(target, loadedTabs) < 0 && url) {
        $.get(url, function(data) {
            $(target).html(data);
            initializeActions();
        });
        loadedTabs[loadedTabs.length] = target;
    }
    $(elem).tab('show');
    return false;
}

var loadedTabs = [];

$(document).ready( function() {
    // Delete links.
    $('.delete').click(function() {
        return confirm('Delete this ' + $(this).attr('text') + '? You cannot undo this action.');
    });

    $('#sidemenu a').click(function (e) {
        showTab($(this));
    });
    var hash = window.location.hash.substring(1);
    if (!hash) {
        hash = 'general';
    }
    showTab($('#sidemenu a[href="#' + hash + '"]'));
    initializeActions();

    if ($('#activate').hasClass('btn-warning')) {
        $('#deactivatedStatus').hide();
    }
    $('#activate').on('click', function(e) {
        e.preventDefault();
        var url = jsRoutes.controllers.Brands.activation($(this).data('id')).url;
        var active = $(this).hasClass('btn-success');
        $.post(url, {active: active}, function(data, textStatus, xhr) {
            switchState(active);
        });
    });

});


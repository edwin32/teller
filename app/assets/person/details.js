/*
 * Happy Melly Teller
 * Copyright (C) 2013 - 2015, Happy Melly http://www.happymelly.com
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

var uploadedPhoto = null;

function switchActivePhoto(object) {
    $('#choosePhotoContent').find('.option').removeClass('active');
    $(object).addClass('active');
}

function updateReason() {
    var url = jsRoutes.controllers.Members.updateReason(getPersonId()).url;
    $.post(url, {reason: $('#reason').val()}, null, "json").done(function(data) {
        $('#reasonDialog').modal('hide');
        $('#reasonToJoin').html(data.message);
        reloadCompletionWidget();
    }).fail(function(jqXHR, status, error) {
    });
}

/**
 * Uploads custom photo and updates photo
 */
function updatePhoto() {
    var url = jsRoutes.controllers.ProfilePhotos.update(getPersonId()).url;
    var object = $('#choosePhotoContent').find('.active');
    var type = $(object).attr('id');
    var src = $(object).find('img').attr('src');
    if (type == "custom" && uploadedPhoto != null) {
        $('#saveLink').text('Uploading...');
        uploadedPhoto.submit();
    }
    $.post(url, { type: type }, null, "json").done(function(data) {
        $('#photoDialog').modal('hide');
        $('#stub').hide();
        $('#real').find('img').attr('src', src);
        $('#real').show();
        $('.photo-block').addClass('real');
        reloadCompletionWidget();
    }).fail(function(jqXHR, status, error) {
    });
}

function showSelectPhotoForm() {
    $.get(jsRoutes.controllers.ProfilePhotos.choose(getPersonId()).url, function(data) {
        $('#choosePhotoContent').html(data);
        $('#choosePhotoContent .option').on('click', function(e) {
            switchActivePhoto($(this));
        });
        $('#saveLink').on('click', updatePhoto);
        setupCustomPhotoActions();
        initializeFileUploadField();
    });
}

function setupCustomPhotoActions() {
    $('#photoUpload').fileupload({
        dataType: 'json',
        disableImageResize: false,
        imageMaxWidth: 300,
        imageMaxHeight: 300,
        imageCrop: false,
        autoUpload: false,
        replaceFileInput: false,
        done: function (e, data) {
            $('#customPhoto').attr('src', data.result.link);
            switchActivePhoto($('#customPhoto').parent());
        }
    }).bind('fileuploadadd', function (e, data) {
        uploadedPhoto = data;
        $('#customPhoto').attr('src', URL.createObjectURL(data.files[0]));
        $('#customPhoto').addClass('photo');
    });
}

/**
 * Shows a table with links if the table has at least one record and hides
 * tables with no records
 */
function showHideMaterialTables() {
    $('.materials').each(function() {
        if ($(this).find('td').length == 0) {
            $(this).hide();
        } else {
            $(this).show();
        }
    })
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
    $('.sidemenu').find('li').removeClass('active');
    $(elem).tab('show');
    return false;
}

/**
 * Deletes experiment
 * @param {int} experimentId
 * @param {string} url
 */
function deleteExperiment(experimentId, url) {
    $.ajax({
        type: "DELETE",
        url:url,
        dataType: "json"
    }).done(function(data) {
        $('div[data-id="' + experimentId + '"]').remove();
    }).fail(function(jqXHR, status, error) {
        //empty
    });
    return false;
}

/**
 * Updates the view after the material was added
 * @param materialId {int} Material id
 * @param brandId {int} Brand id
 * @param personId {int} Person id
 * @param materialType {string} material type
 * @param material {string} material
 */
function addMaterial(materialId, brandId, personId, materialType, material) {
    $('#materials_' + brandId).append(
        $('<tr>')
            .attr('data-id', materialId)
            .attr('data-personid', personId)
            .append($('<td>').append(materialType))
            .append($('<td>')
                .append($('<a>')
                    .attr('href', material)
                    .append(material)))
            .append($('<td>')
                .append($('<a>')
                    .append('Delete')
                    .addClass('remove font-sm')
                    .attr('href', '#')
                    .attr('data-id', materialId)
                    .attr('data-href', jsRoutes.controllers.Materials.remove(personId, materialId).url)))
    );
    showHideMaterialTables();
}

/**
 * Updates the view after the material was deleted
 * @param materialId {int} Material id
 */
function deleteMaterial(materialId) {
    $('tr[data-id="' + materialId + '"]').remove();
    showHideMaterialTables();
}

/**
 * Updates the view after the endorsement was deleted
 * @param endorsementId {int} Endorsement id
 */
function deleteEndorsement(endorsementId) {
    $('.endorsements').find('div[data-id="' + endorsementId + '"]').remove();
}

function initializeExperienceTabActions() {
    $('.material-form').submit(function(e) {
        var that = $(this);
        $.post($(this).attr("action"), $(this).serialize(), null, "json").done(function(data) {
            addMaterial(data.id, data.brandId, data.personId, data.linkType, data.link);
            $(that).trigger('reset');
        }).fail(function(jqXHR, status, error) {
            if (status == "error") {
                var error = JSON.parse(jqXHR.responseText);
                noty({text: error.message, layout: 'bottom', theme: 'relax', type: 'error'});
            } else {
                var msg = "Internal error. Please try again or contant the support team.";
                noty({text: msg, layout: 'bottom', theme: 'relax', type: 'error'});
            }
        });
        // Prevent the form from submitting with the default action
        return false;
    });
    $('.materials').on('click', 'a.remove', function(e) {
        var materialId = $(this).data('id');
        $.ajax({
            type: "DELETE",
            url: $(this).data('href'),
            dataType: "json"
        }).done(function(data) {
            deleteMaterial(materialId, name);
        }).fail(function(jqXHR, status, error) {
            if (status == "error") {
                var error = JSON.parse(jqXHR.responseText);
                noty({text: error.message, layout: 'bottom', theme: 'relax', type: 'error'});
            } else {
                var msg = "Internal error. Please try again or contant the support team.";
                noty({text: msg, layout: 'bottom', theme: 'relax', type: 'error'});
            }
        });
        return false;
    });
    $('.endorsements').on('click', 'a.remove', function(e) {
        var endorsementId = $(this).data('id');
        $.ajax({
            type: "DELETE",
            url: $(this).data('href'),
            dataType: "json"
        }).done(function(data) {
            deleteEndorsement(endorsementId);
        }).fail(function(jqXHR, status, error) {
            if (status == "error") {
                var error = JSON.parse(jqXHR.responseText);
                noty({text: error.message, layout: 'bottom', theme: 'relax', type: 'error'});
            } else {
                var msg = "Internal error. Please try again or contant the support team.";
                noty({text: msg, layout: 'bottom', theme: 'relax', type: 'error'});
            }
        });
        return false;
    });
    var mouseState = 'up';
    var timerId = 0;
    var activePosition = 0;
    var moveToPosition = 0;
    $('.endorsement').on('mousedown', function(e) {
        mouseState = 'down';
        var className = 'selected';
        $('.endorsement').removeClass(className);
        $(this).addClass(className);
        $('#toolTip').html($(this).find('.tooltip-content').html());
        var that = $(this);
        timerId = window.setTimeout(function() {
            if (mouseState == 'down') {
                $("#toolTip").css({top: e.clientY, left: e.clientX}).show();
                activePosition = $(that).attr('data-position');
            }
        }, 500);
        return false;
    });
    $('.endorsement').on('mouseover', function(e) {
        var position = $(this).attr('data-position');
        if (position != moveToPosition) {
            $('.highlighted-top').removeClass('highlighted-top');
            $('.highlighted-bottom').removeClass('highlighted-bottom');
            moveToPosition = position;
            if (mouseState == 'down' && activePosition != 0 && moveToPosition != activePosition) {
                if (moveToPosition < activePosition) {
                    $(this).addClass('highlighted-top');
                } else {
                    $(this).addClass('highlighted-bottom');
                }
            }
        }
    });

    $('.endorsements').on('mouseup', function(e) {
        if (activePosition != 0 && moveToPosition != activePosition) {
            if (moveToPosition < activePosition) {
                $('[data-position=' + activePosition + ']').insertBefore($('[data-position=' + moveToPosition + ']'));
            } else {
                $('[data-position=' + activePosition + ']').insertAfter($('[data-position=' + moveToPosition + ']'));
            }
            recalculatePositions();
        }
        endorsementCleanUp();
        return false;
    });
    $('.endorsements').on('mousemove', function(e) {
        if (mouseState == 'down') {
            $("#toolTip").css({top: e.clientY, left: e.clientX}).show();
        }
        return false;
    });
    $('.endorsements').on('mouseleave', function(e) {
        $('.endorsement').removeClass('selected');
        endorsementCleanUp();
    });

    function recalculatePositions() {
        var position = 1;
        var changedRecords = [];
        $('.endorsements').find('.endorsement').each(function(e) {
            if ($(this).attr('data-position') != position) {
                changedRecords[changedRecords.length] = {
                    id: parseInt($(this).attr('data-id')),
                    position: position
                };
            }
            $(this).attr('data-position', position);
            position += 1;
        });
        if (changedRecords.length > 0) {
            var url = jsRoutes.controllers.Endorsements.updatePositions($('#personId').val()).url;
            $.post(url, { positions: JSON.stringify(changedRecords) }, function(e) {
            });
        }
    }

    function endorsementCleanUp() {
        mouseState = 'up';
        window.clearTimeout(timerId);
        $('#toolTip').hide();
        activePosition = 0;
        moveToPosition = 0;
    }
    showHideMaterialTables();
}

function initializeActions() {
    $('#experimentList').on('click', 'a.remove', function(e) {
        return deleteExperiment($(this).data('id'), $(this).data('href'));
    });
    $('#experimentList').on('click', 'a.deletePicture', function(e) {
        var experimentId = $(this).data('id');
        var that = this;
        $.ajax({
            type: "DELETE",
            url: $(this).data('href'),
            dataType: "json"
        }).done(function(data) {
            $('div[data-id="' + experimentId + '"]').find('.picture').remove();
            $(that).remove();
        }).fail(function(jqXHR, status, error) {
            //empty
        });
        return false;
    });
    $('#saveReason').on('click', updateReason);
    $('#formSwither').on('click', function(e) {
        $('.signature').hide();
        $('.no-signature').show();
    });
    $('#signature').on('change', function(e) {
        if ($(this).val().length > 0) {
            $('#signatureUploader').prop('disabled', false);
        }
    });
    $('#signatureUploader').prop('disabled', true);
    initializeFileUploadField();
    initializeExperienceTabActions();
}

var loadedTabs = [];

$(document).ready( function() {

    // Delete links.
    $('form.delete').submit(function() {
        return confirm('Delete this ' + $(this).attr('text') + '? You cannot undo this action.');
    });

    $('.payments').dataTable( {
        "sPaginationType": "bootstrap",
        "order": [[ 2, "desc" ]],
        "columnDefs": [
            { "orderable": false, "targets": 0 },
            { "orderable": false, "targets": 1 }
        ],
        "bFilter": false,
        "bInfo": false,
        "bLengthChange": false,
        "bPaginate": false
    });

    $('#sidemenu a').click(function (e) {
        e.preventDefault();
        showTab($(this));
    });
    var hash = window.location.hash.substring(1);
    if (!hash) {
        hash = 'personal-details';
    }
    showTab($('#sidemenu a[href="#' + hash + '"]'));
    initializeActions();

    $('[data-toggle="tooltip"]').tooltip();
    $('#saveReason').on('click', updateReason);

    $('.choosePhotoLink').on('click', function(e) {
        showSelectPhotoForm();
    });

    $('#btnPhotoDelete').on('click', function(e) {
        $.ajax({
            type: "DELETE",
            url: $(this).data('href'),
            dataType: "json"
        }).done(function(data) {
            $('.photo-block').removeClass('real');
            $('#real').hide();
            $('#stub').show();
            reloadCompletionWidget();
        });
        return false;
    });
});


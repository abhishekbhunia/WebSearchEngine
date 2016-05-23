$(function (){
  $('#submit').on('click', function() {
    var $searchQuery = $('#searchbox').val();
    $('#submitButton').hide();
    $('#resultButtons').hide();
    $('#loaderPic').show();
    $('#lingo-results').hide();
    $('#kmeans-result').hide();
    $('#stc-results').hide();
    //alert($searchQuery);
    $.ajax({
      type: 'POST',
      url: './samplecgipython.cgi',
      dataType: 'html',
      data: {searchQuery:$searchQuery},
      success: function(response) {
        //alert(response);
        $('#submitButton').show();
        $('#loaderPic').hide();
        $('#resultButtons').show();
        var lingo_response = response.substring(response.indexOf("lstartidx")+9,response.indexOf("lendidx"));
        $('#lingo-results').html(lingo_response);
    
        var kmeans_response = response.substring(response.indexOf("kstartidx")+9,response.indexOf("kendidx"));
        $('#kmeans-results').html(kmeans_response);

        var stc_response = response.substring(response.indexOf("sstartidx")+9,response.indexOf("sendidx"));
        $('#stc-results').html(stc_response);
      },
      error: function() {
        alert('error updating result');
      }

    });
  });

  $('#lingoButton').on('click', function() {
      $('#kmeans-results').hide();
      $('#stc-results').hide();
      $('#lingo-results').toggle();
    });

  $('#kmeansButton').on('click', function() {
      $('#lingo-results').hide();
      $('#stc-results').hide();
      $('#kmeans-results').toggle();
    });

  $('#stcButton').on('click', function() {
      $('#lingo-results').hide();
      $('#kmeans-results').hide();
      $('#stc-results').toggle();
    });
});

/* $body = $('body');
  $(document).on({
    ajaxStart: function() {
                            $body.addClass('loading');
                          },
    ajaxStop: function() {
                           $body.removeClass('loading');
    }
  });
*/
/*
$('#loaderPic').hide().ajaxStart( function() {
    $(this).show();  // show Loading Div
  }).ajaxStop ( function(){
    $(this).hide(); // hide loading div
});


$('#submitButton').hide().ajaxStart( function() {
    $(this).hide();  // show Loading Div
  }).ajaxStop ( function(){
    $(this).show(); // hide loading div
});
*/



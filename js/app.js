window.onload = function () {
    /*
    * v1.0
    * 2.可以前后选择url
    * 3.日志区默认下拉到最后
    *
    * */
    var TEMP = null;
    var $url = document.getElementById('url');
    var $tagSelect = document.getElementById('tag');
    var $confirmBtn = document.getElementById('confirm');
    var $submitBtn = document.getElementById('submit');
    var $nextBtn = document.getElementById('next_one');
    var $iframe = document.getElementsByTagName('iframe')[0];
    var $nextOneDisable = true;
    var $batchEnded = false;
    var jsonResult = null;
    var xmlhttp = null;
    var jsonData = null;
    var _keys = null;
    var keysLength = null;

    //Logger Object--日志打印
    var log = document.getElementById('logger-area');
    var originalLogText = log.innerHTML;
    var $p, $txt;
    var logger = {
        show: function (msg) {
            console.log(msg);
            alert(msg);
        },
        add: function (msg) {
            $p = document.createElement('p');
            $txt = document.createTextNode(msg);
            $p.appendChild($txt);
            log.appendChild($p);
        },
        clear: function () {
            log.innerHTML = originalLogText;
        },
    };
    //click and show the details--显示日志详情
    log.addEventListener('click', function (e) {
        if (e.target.tagName.toLowerCase() === 'p') {
            logger.show(e.target.innerHTML);
        }

    }, false);

    //Select Object
    $tagSelect.addEventListener('change', function (e) {

    });

    //whether next one
    $nextBtn.addEventListener('click', function () {
        if(!$nextOneDisable && !$batchEnded) {
            if (TEMP.nextIndex < keysLength){
                TEMP = urlUtil.displayByKey(TEMP.nextKey);
            }else{
                $batchEnded = true;
            }
            $nextOneDisable = true;
        }else if ($nextOneDisable){
            alert("Cannot goto next one: current one NOT tagged.");
        }else if($batchEnded) {
            alert("All the tags tagged: Please submit");
        }else {
            alert("BUG at nextBtn!!");
        }
    });

    //选择确认
    $confirmBtn.addEventListener('click', function () {
        var logLength = log.childElementCount - 1;
        if (logLength >= 7){
            logger.clear();
        }
        if(!$batchEnded){
            var $selectedText = $tagSelect.options[$tagSelect.selectedIndex].text;
            logger.add('url: ' + $url.value + '  ' + '--> tag: ' + $selectedText);
            jsonResult[TEMP.curKey] = $selectedText;//add to result
        }else{
            alert("Completed this round.Press submit button to submit");
        }
        $nextOneDisable = false;
    });

    //提交确认
    $submitBtn.addEventListener('click', function () {
        logger.add("    Tagging completed. Going to submit tags:\n");
        logger.add(JSON.stringify(jsonResult));
        send_back_to_server();
    });

    //Url Object--网址显示
    var urlUtil = {
        displayByKey: function (key) {
            if (jsonData[key]) {
                $url.value = jsonData[key];
                $iframe.src = $url.value;
                var index = _keys.indexOf(key);
                if (index !== -1) {
                    return {
                        'curKey': _keys[index],
                            'curIndex': index,
                            'nextKey': _keys[index + 1],
                            'nextIndex': index + 1
                    };
                }else{
                    logger.add("BUG!! jsonData index out of range!");
                }
            }
        },
        searchAndBind: function (url) {

        }
    };

    function get_data () {
        $nextOneDisable = true;
        $batchEnded = false;
        jsonResult = {};//结果
        xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function() {
            if(this.readyState == 4 && this.status == 200) {
                jsonData = JSON.parse(xmlhttp.responseText);
                _keys = Object.keys(jsonData);//json的key
                keysLength = _keys.length;

                //show the first one
                TEMP = urlUtil.displayByKey(_keys[0]);
            }
        }
        xmlhttp.open("GET", "/setTag.php",true);
        xmlhttp.send();
    }

    function send_back_to_server () {
        xmlhttp = new XMLHttpRequest();
        xmlhttp.onreadystatechange = function() {
            if(this.readyState == 4 && this.status == 200){
                get_data();
            }
        }
        xmlhttp.open("POST", "/setTag.php", true);
        //let server know the encoding we used for the request body
        //urlencode: encode the POST data as in url(convention)
        xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        var $jsr = "result=".concat(JSON.stringify(jsonResult));
        xmlhttp.send($jsr);

        //if send_back_to_server, then clear the content
        $nextOneDisable = true;
        $batchEnded = false;
        jsonResult = null;
        xmlhttp = null;
        jsonData = null;
        _keys = null;
        keysLength = null;
    }

    get_data();
};

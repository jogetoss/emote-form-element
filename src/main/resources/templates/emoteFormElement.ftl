<#if !(request.getAttribute("org.joget.marketplace.EmoteFormElement_EDITABLE")??) >
    <#if !includeMetaData>
        <#if id?has_content>
            <#--  When form is submitted  -->
            <div class="form-cell" ${elementMetaData!}>
                <div class="emoji-container">
                    <button type="button" class="btn btn-primary trigger emojiTriggerBtn" id="emoji-btn">${btnIcon!}</button>
                    <div class="reaction-container" id="reaction-container">
                        <#list emoteList as row>
                            <#if row.emote! != ''>
                                <button data-tooltip="${row.emote!} reacted by ${row.createdByName!}" type="button" class="btn btn-default emoteBtn custom-tooltip ${row.sameUser?then('reacted', '')}" onclick="removeEmoji(this, '${row.emote!}')">${row.emote!} ${row.count!}</button>
                            </#if>
                        </#list>
                    </div>
                </div>
            </div>
        </#if>
    <#else>
        <#--  Form Builder  -->
        <div class="form-cell" ${elementMetaData!}>
            <button type="button" class="btn btn-primary trigger emojiTriggerBtn" style="background-color:${btnBgColor!};color:${btnIconColor!};" id="emoji-btn">${btnIcon!}</button><br>
            <button type="button" class="btn btn-default" style="border: 1px solid #a6a6a6 !important;border-radius: 10px;background-color:${reactbtnBgColor!};color:${reactbtnTextColor!};">Unreacted</button>
            <button type="button" class="btn btn-default" style="border: 1px solid #a6a6a6 !important;border-radius: 10px;background-color:${reactbtnReactedBgColor!};color:${reactbtnReactedTextColor!};">Reacted</button>
        </div>
    </#if>
    <#if id?has_content>
    <style>
        .emojiTriggerBtn{
            background-color: ${btnBgColor} !important;
            color: ${btnIconColor} !important;
        }

        .emoteBtn {
            border: 1px solid #a6a6a6 !important;
            border-radius: 10px !important;
            background-color: ${reactbtnBgColor} !important;
            color: ${reactbtnTextColor} !important;
        }

        .emoteMargin{
            margin-right: 3px !important;
        }

        .reacted {
            background-color: ${reactbtnReactedBgColor} !important;
            color: ${reactbtnReactedTextColor} !important;
        }

        .custom-tooltip {
            position: relative;
            cursor: pointer;
        }

        .custom-tooltip::after {
            border: 1px solid #a6a6a6 !important;
            content: attr(data-tooltip);
            visibility: hidden;
            width: auto;
            background-color: #f0f0f0;
            color: black;
            text-align: center;
            border-radius: 6px;
            padding: 10px;
            position: absolute;
            z-index: 1001;
            bottom: 125%; /* Position the tooltip above the button */
            left: 50%;
            transform: translateX(-50%);
            opacity: 0;
            transition: opacity 0.2s ease-in-out; /* Faster display */
            font-size: 15px;
            white-space: nowrap; 
        }

        .custom-tooltip:hover::after {
            visibility: visible;
            opacity: 1;
        }
    </style>
    <script src="${request.contextPath}/plugin/org.joget.marketplace.EmoteFormElement/picmo/emoji-button.min.js"></script>
    <script type="text/javascript">
        var picker = new EmojiButton();
        var trigger = document.querySelector('.trigger');
        var buttonContainer = document.getElementById('reaction-container');

        picker.on('emoji', selection => {
             ajaxCallToWebService(selection, "add");
        });

        trigger.addEventListener('click', () => picker.togglePicker(trigger));

        function removeEmoji(buttonElement, selection) {
            if (buttonElement.classList.contains('reacted')) {
                ajaxCallToWebService(selection, "remove");
            } else {
                ajaxCallToWebService(selection, "add");
            }
        }

        function ajaxCallToWebService(selection, action){
              $.ajax({
                    url: '${element.serviceUrl!}',
                    type: 'POST',
                    data: {
                        id: '${id}',
                        clickedValue: selection,
                        config : '${element.configString!}',
                        action: action
                    },
                    success: function (data) {
                        var html = '';

                        data.emoteList.forEach(function(row) {
                            if (row.emote) { 
                                var buttonClass = row.sameUser ? 'reacted' : '';
                                var tooltip = row.emote + ' reacted by ' + row.createdByName;
                                
                                html += '<button data-tooltip="' + tooltip + '" type="button" class="btn btn-default emoteBtn emoteMargin custom-tooltip ' + buttonClass + '" onclick="removeEmoji(this, \'' + row.emote + '\')">' + row.emote + ' ' + row.count + '</button>';
                            }
                        });

                        $('#reaction-container').html(html);
                    }
                });
        }
    </script>
    </#if>
</#if>

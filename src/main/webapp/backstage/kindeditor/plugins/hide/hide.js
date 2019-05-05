KindEditor.plugin('hide', function(K) {
	//self为编辑器(Editor) API
	var self = this, name = 'hide',
	
	tabIndex = K.undef(0, 0);

	/**
	 * 替换标签
	 * html html内容
	 * tag 标签名称
	 * className css样式名称
	 * inputValue 替换标签值
	 */
	function replaceTab(html,tag,className,inputValue){
		var node = document.createElement("div");
		node.innerHTML = html;
		
		getChildNode(node,tag,className,inputValue);
		return node.innerHTML;
	}
	
	/**
	 * 递归获取所有的子节点
	 * node 节点
	 * tag 标签名称
	 * className css样式名称
	 * inputValue 替换标签值
	 * 
	 */
	function getChildNode(node,tag,className,inputValue){
        //先找到子节点
        var nodeList = node.childNodes;
        for(var i = 0;i < nodeList.length;i++){
            //childNode获取到到的节点包含了各种类型的节点
            //但是我们只需要元素节点  通过nodeType去判断当前的这个节点是不是元素节点
            var childNode = nodeList[i];
            
            //判断是否是元素节点。如果节点是元素(Element)节点，则 nodeType 属性将返回 1。如果节点是属性(Attr)节点，则 nodeType 属性将返回 2。
            if(childNode.nodeType == 1){
            	if(childNode.nodeName.toLowerCase() == tag.toLowerCase() && 
            			childNode.getAttribute("class") == className){
            		childNode.setAttribute("input-value",inputValue);
            		 
            	}
                getChildNode(childNode,tag,className,inputValue);
            }
        }
    }
	

    // 点击图标时执行
	self.plugin.hideDialog = function(options) {
		
		var lang = self.lang(name + '.'),
		selectedNode = options.selectedNode,
		
		html = [
				'<div style="padding:20px;height:130px;">',
					//tabs
					'<div class="tabs"></div>',
					//选项1
					'<div class="tab1" style="display:none;">',
						//密码
						'<div class="ke-dialog-row">',
						'<label for="inputValue_10" style="width:60px;">密码</label>',
						'<input type="text" id="inputValue_10" class="ke-input-text ke-input-number" name="inputValue_10" value="'+options.hidePassword+'" maxlength="20" style="width:200px;" /> ',
						'</div>',
					'</div>',
					//选项2
					'<div class="tab2" style="display:none;">',
					
					'</div>',
					
					//选项3
					'<div class="tab3" style="display:none;">',
						//超过积分
						'<div class="ke-dialog-row">',
						'<label for="inputValue_30" style="width:60px;">积分超过</label>',
						'<input type="text" id="inputValue_30" class="ke-input-text ke-input-number" name="inputValue_30" value="'+options.hideMinPoint+'" maxlength="8" style="width:130px;" /> 以上可见 ',
						'</div>',
					'</div>',
					//选项4
					'<div class="tab4" style="display:none;">',
						//积分购买
						'<div class="ke-dialog-row">',
						'<label for="inputValue_40" style="width:60px;">需要支付</label>',
						'<input type="text" id="inputValue_40" class="ke-input-text ke-input-number" name="inputValue_40" value="'+options.hidePoint+'" maxlength="8" style="width:130px;" /> 积分可见',
						'</div>',
					'</div>',
					//选项5
					'<div class="tab5" style="display:none;">',
						//余额购买
						'<div class="ke-dialog-row">',
						'<label for="inputValue_50" style="width:60px;">需要支付</label>',
						'<input type="text" id="inputValue_50" class="ke-input-text ke-input-number" name="inputValue_50" value="'+options.hideAmount+'" maxlength="9" style="width:130px;" /> 元费用可见',
						'</div>',
					'</div>',
					'<div>',
						//提示
						'<div style="color: #747474">',
						'提示：Shift + 回车 换行不换段',
						'</div>',
					'</div>',
				'</div>'
			].join(''),
			
			
		dialog = self.createDialog({
			name : name,
			width : 450,
			title : self.lang(name),
			body : html,
			yesBtn : {
				name : self.lang('yes'),
				click : function(e) {
					
					html = "";
					var password = K.trim(passwordBox.val()),
					minPoint = K.trim(minPointBox.val()),
					point = K.trim(pointBox.val()),
					amount = K.trim(amountBox.val());
					
					if(tabIndexBox == 0){//输入密码可见
						if (password == "") {
							alert("请输入密码");
							passwordBox[0].focus();
							return;
						}
					}else if(tabIndexBox == 1){//回复话题可见
						
					}else if(tabIndexBox == 2){//超过积分可见
						if (minPoint == "" || !/^[0-9]*[1-9][0-9]*$/.test(minPoint)) {//正整数
							alert("请输入大于0的数字");
							minPointBox[0].focus();
							return;
						}
					}else if(tabIndexBox == 3){//积分购买可见
						if (point == "" || !/^[0-9]*[1-9][0-9]*$/.test(point)) {//正整数
							alert("请输入大于0的数字");
							pointBox[0].focus();
							return;
						}
					}else if(tabIndexBox == 4){//余额购买可见  正整数,也可接收正浮点数，两位小数
						if (amount == "" || !/^(([1-9]\d*)(\.\d{1,2})?)$|(0\.0?([1-9]\d?))$/.test(amount)) {
							alert("请输入大于0的金额");
							amountBox[0].focus();
							return;
						}
					}
	
					
					if(selectedNode != null){//更新
						if(tabIndexBox == 0){//输入密码可见
							selectedNode.attr('class','inputValue_10');
							selectedNode.attr('hide-type',10);
							selectedNode.attr('input-value',password);
						}else if(tabIndexBox == 1){//回复话题可见
							selectedNode.attr('class','inputValue_20');
							selectedNode.attr('hide-type',20);
							selectedNode.attr('input-value','');
						}else if(tabIndexBox == 2){//超过积分可见
							selectedNode.attr('class','inputValue_30');
							selectedNode.attr('hide-type',30);
							selectedNode.attr('input-value',minPoint);
						}else if(tabIndexBox == 3){//积分购买可见
							selectedNode.attr('class','inputValue_40');
							selectedNode.attr('hide-type',40);
							selectedNode.attr('input-value',point);
						}else if(tabIndexBox == 4){//余额购买可见
							selectedNode.attr('class','inputValue_50');
							selectedNode.attr('hide-type',50);
							selectedNode.attr('input-value',amount);
						}
						self.hideDialog();
					}else{//添加
						if(tabIndexBox == 0){//输入密码可见
							html = "<hide class='inputValue_10' hide-type='10' input-value='"+password+"'></hide>";
						}else if(tabIndexBox == 1){//评论话题可见
							html = "<hide class='inputValue_20' hide-type='20' ></hide>";
						}else if(tabIndexBox == 2){//达到等级可见
							html = "<hide class='inputValue_30' hide-type='30' input-value='"+minPoint+"'></hide>";
						}else if(tabIndexBox == 3){//积分购买可见
							html = "<hide class='inputValue_40' hide-type='40' input-value='"+point+"'></hide>";
						}else if(tabIndexBox == 4){//余额购买可见
							html = "<hide class='inputValue_50' hide-type='50' input-value='"+amount+"'></hide>";
						}
						self.insertHtml(html).hideDialog();
						
					}
					
					if(tabIndexBox == 0){//输入密码可见
						//替换标签
						var htmlValue = replaceTab(self.html(),"hide","inputValue_10",""+password+"");
						self.html(htmlValue);
					}else if(tabIndexBox == 1){//回复话题可见
						
					}else if(tabIndexBox == 2){//超过积分可见
						//替换标签
						var htmlValue = replaceTab(self.html(),"hide","inputValue_30",""+minPoint+"");
						self.html(htmlValue);
					}else if(tabIndexBox == 3){//积分购买可见
						var htmlValue = replaceTab(self.html(),"hide","inputValue_40",""+point+""); 
						self.html(htmlValue);
					}else if(tabIndexBox == 4){//余额购买可见
						var htmlValue = replaceTab(self.html(),"hide","inputValue_50",""+amount+""); 
						self.html(htmlValue);
					}

					
				},
				beforeRemove : function() {
					passwordBox.unbind();//移除所有事件函数
					minPointBox.unbind();
					pointBox.unbind();
					amountBox.unbind();
					
				}
			}
		}),
		div = dialog.div;
		
		
		passwordBox = K('[name="inputValue_10"]', div),//密码
		minPointBox = K('[name="inputValue_30"]', div),//需要积分
		pointBox = K('[name="inputValue_40"]', div),//积分
		amountBox = K('[name="inputValue_50"]', div);//金额
		tabIndexBox = 0;
		
		
		if(options.hideVisibleType == 10){//输入密码可见
			tabIndex = 0;
		}else if(options.hideVisibleType == 20){//回复话题可见
			tabIndex = 1;
		}else if(options.hideVisibleType == 30){//超过积分可见
			tabIndex = 2;
		}else if(options.hideVisibleType == 40){//积分购买可见
			tabIndex = 3;
		}else if(options.hideVisibleType == 50){//余额购买可见
			tabIndex = 4;
		}
		

		var tabs;
		tabs = K.tabs({
			src : K('.tabs', div),
			afterSelect : function(i) {
				tabIndexBox = i;
			}
		});
		tabs.add({
			title : '输入密码可见',
			panel : K('.tab1', div)
		});
		tabs.add({
			title : '回复话题可见',
			panel : K('.tab2', div)
		});
		/**
		tabs.add({
			title : '超过积分可见',
			panel : K('.tab3', div)
		});
		tabs.add({
			title : '积分购买可见',
			panel : K('.tab4', div)
		});
		tabs.add({
			title : '余额购买可见',
			panel : K('.tab5', div)
		});**/
		tabs.select(tabIndex);

		K('.tab'+(tabIndex+1), div).show();
		
	};
	self.plugin.hide = {
		edit : function() {
			var hide = self.plugin.getSelectedHide();
			
			self.plugin.hideDialog({
				
				selectedNode: hide ? hide : null,
				hideInputValue : hide ? hide.attr('input-value') : '',
				hideVisibleType : hide ? hide.attr('hide-type') : '',
				hidePassword : (hide && hide.attr('hide-type') == 10 )? hide.attr('input-value') : '',	
				hideMinPoint : (hide && hide.attr('hide-type') == 30 )? hide.attr('input-value') : '',	
				hidePoint : (hide && hide.attr('hide-type') == 40 )? hide.attr('input-value') : '',
				hideAmount : (hide && hide.attr('hide-type') == 50 )? hide.attr('input-value') : '',		
						
			});
		},
		'delete' : function() {
			
			var hide = self.plugin.getSelectedHide();
			
			hide.remove();
			self.addBookmark();
		}
	};
	
	//self.clickToolbar(name, self.plugin.hide.edit);
	self.clickToolbar(name, function() {
		self.plugin.hideDialog({
			selectedNode: null,
			hideInputValue : '',
			hideVisibleType : '',
			hidePassword : '',	
			hideMinPoint : '',	
			hidePoint : '',
			hideAmount : ''	
		});
	});
	

	
});



<template>
  <v-container>
    <alert-dialog></alert-dialog>
    <full-dialog></full-dialog>
    <v-row align="center" justify="space-around">
      <slot name="searchslot"></slot>
      <v-btn text :loading="querying" @click="onSearch(0)">
        <v-icon color="primary" large>mdi-card-search</v-icon>
      </v-btn>
      <v-btn icon @click="routeToUpdateOrCreateScreen(false)">
        <v-icon  color="primary" large>mdi-sticker-plus-outline</v-icon>
      </v-btn>
    </v-row>
    <v-row>
      <v-expansion-panels hover light v-model="panel" multiple>
        <v-expansion-panel v-for="(item,i) in rows" :key="i" id="app">
          <v-expansion-panel-header>
            <div align="start">
              <v-badge color="red" :content="item.tip" v-if="typeof item.tip !== 'undefined'&& typeof item.tipType !== 'undefined'">
                <v-icon color="primary" >{{comutil.getTipIconName(item.tipType)}}</v-icon>
              </v-badge>
                <v-icon color="primary" v-else>{{comutil.getTipIconName(1)}}</v-icon>
              <span v-html="meta.row(item)" />
            </div>
          </v-expansion-panel-header>
          <v-expansion-panel-content>
            <div v-html="meta.content(item)"></div>
            <v-divider></v-divider>
            <v-row justify="end">
              <div style="margin-top: 10px">
                <v-btn
                  icon
                  id="detailbtn"
                  @click="showDetailScreen(i,meta.id(item),meta.title(item))"
                >
                  <v-icon large color="primary">mdi-subtitles-outline</v-icon>
                </v-btn>
                <slot name="btnslot"></slot>
                <!-- <v-btn
                  icon
                  @click="routeToUpdateOrCreateScreen(false,i,meta.id(item),meta.title(item))"
                >
                  <v-icon large color="primary">mdi-sticker-plus-outline</v-icon>
                </v-btn> -->
                <v-btn
                  icon
                  @click="routeToUpdateOrCreateScreen(true,i,meta.id(item),meta.title(item))"
                >
                  <v-icon large color="primary">mdi-pencil</v-icon>
                </v-btn>
                <v-btn icon @click="confirmDel(i,meta.id(item),meta.title(item))">
                  <v-icon large color="primary">mdi-delete-forever</v-icon>
                </v-btn>
              </div>
            </v-row>
          </v-expansion-panel-content>
        </v-expansion-panel>
      </v-expansion-panels>
    </v-row>
  </v-container>
</template>
<script>
export default {
  props: ['meta'],
  data() {
    return {
      querying: false,
      panel: [],
      rows: null
    }
  },
  methods: {
    confirmDel(index, theId, title) {
      var alert = {
        title: this.comutil.OptInfo.delete.title,
        itemInfo: title,
        content: this.comutil.OptInfo.delete.content,
        alType: this.comutil.AlertType.WARN,
        callBack: this.deleteItem,
        callBackParams: { index: index, id: theId, title: title }
      }
      console.log('confirm delete row  index ' + index + ' id:' + theId)
      this.comutil.Alert.showAlert(alert)
    },
    showDetailScreen(index, theId, title) {
      //  this.$router.push()
      //  this.comutil.showAlert
      this.comutil.CURDDialog.showDialog({
        vueComponent: this.meta.detailurl,
        title: title
      })
    },
    routeToUpdateOrCreateScreen(update, index, theId, title) {
      let isprocess = false
      let url = this.meta.createurl
      let dialogtitle = this.meta.createtitle
      if (update) {
        url = this.meta.updateurl
        dialogtitle = title
        isprocess = true
      }

      this.comutil.CURDDialog.showDialog({
        itemIndex: index,
        itemId: theId,
        title: dialogtitle,
        vueComponent: url,
        isprocess: isprocess
      })
    },
    async deleteItem(selectedParm) {
      console.log(
        'do syn delete row ' + selectedParm.id + ' ,url  ' + this.meta.deleteurl
      )
      await new Promise(resolve => setTimeout(resolve, 3000))
      // let jsObj = {}
      // jsObj.id = selectedParm.id
      try {
          let { data } = await this.axioscall.post(this.meta.deleteurl, selectedParm.id)
          console.log('DomainListPage_methods:deleteItem_responsedata---' + JSON.stringify(data))
          // 此处返回值比较判断可用预先定义替换字符串
          if (data.retCode === 0) {
          this.$data.rows.splice(selectedParm.index, 1)
          this.comutil.MessageBox.show('id:' + selectedParm.id + '删除成功！')
          return true
          } else {
            this.comutil.MessageBox.show('删除失败，请重试')
            return false
          }
      } catch (e) {
        console.log(' exec call error ' + e)
        this.comutil.MessageBox.show(e)
         return false
      } finally {

      }
    },
    async onSearch(pagenum) {
      this.meta.isLoading = true
      let queryparam = this.meta.queryparams()
      console.log('DomainListPage_methods:onSearch_queryparams---' + JSON.stringify(queryparam))
      // this.comutil.MessageBox.show(JSON.stringify(queryparam))
      this.querying = true
      let mydata = this.$data
      try {
        let { data } = await this.axioscall.post(this.meta.queryurl, queryparam)
        // 打印内容可能会比较多，建议注释
        // console.log('DomainListPage_methods:onSearch_responsedata---' + JSON.stringify(data.data))
        if (pagenum === 0) {
        mydata.rows = data.data
        this.meta.pagenum = pagenum + 1
        // 点击搜索、首次加载滚动置零
        this.meta.deltaY = 0
        } else {
          mydata.rows = mydata.rows.concat(data.data)
          this.meta.pagenum = pagenum + 1
        }
         this.meta.isLoading = false
          this.comutil.MessageBox.show('加载完成')
      } catch (e) {
        this.meta.isLoading = false
        console.log(' exec call error ' + e)
        this.comutil.MessageBox.show(e)
      } finally {
        this.meta.isLoading = false
        this.querying = false
      }
    },
    onScroll() {
      // 如果数据有在加载中则这次请求退出
       if (this.meta.isLoading) return
        console.log('loadmore')
       let innerHeight = document.querySelector('#app').scrollHeight
       let outerHeight = document.documentElement.clientHeight
       let scrollTop = document.documentElement.scrollTop
                    console.log(innerHeight + ' ' + outerHeight + '' + scrollTop)
        if ((innerHeight + outerHeight / 10) < (outerHeight + scrollTop)) {
          // 加载更多操作
          console.log('loadmore')
         this.onSearch(this.meta.pagenum)
       }
    },
     handleScroll (e) {
      // 如果数据有在加载中则这次请求退出
      if (this.meta.isLoading) return
       let innerHeight = document.querySelector('#app').scrollHeight
       let outerHeight = document.documentElement.clientHeight
       let scrollTop = e.deltaY + this.meta.deltaY
       // 滚动到顶部置零
       if (scrollTop <= 0) {
          scrollTop = 0
       }
       this.meta.deltaY = scrollTop
       console.log(scrollTop)
     if ((innerHeight + outerHeight / 10) < (outerHeight + scrollTop)) {
          // 加载更多操作
          console.log('loadmore')
      this.onSearch(this.meta.pagenum)
     }
        //  console.log(e.deltaY)
    },
    _isMobile() {
   let flag = navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i)
    return flag
    }
  },
  mounted: function() {
    console.log('DomainListPage mounted')
    window.onbeforeunload = e => {
       window.removeEventListener('scroll', this.onScroll)
        // chrome and ie
        window.removeEventListener('mousewheel', this.handleScroll, false)
        // firefox
        window.removeEventListener('DOMMouseScroll', this.handleScroll, false)
    }
    this.$data.rows = this.meta.rows
    this.onSearch(0)
      if (this._isMobile()) {
        console.log('mobile')
        window.addEventListener('scroll', this.onScroll)
        } else {
        console.log('pc')
        // chrome and ie
        window.addEventListener('mousewheel', this.handleScroll, false)
        // firefox
        window.addEventListener('DOMMouseScroll', this.handleScroll, false)
      }
  },
  destroyed: function() {
        window.removeEventListener('scroll', this.onScroll)
        // chrome and ie
        window.removeEventListener('mousewheel', this.handleScroll, false)
        // firefox
        window.removeEventListener('DOMMouseScroll', this.handleScroll, false)
  }
}
</script>

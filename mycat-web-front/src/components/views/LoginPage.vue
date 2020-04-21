<template>
  <v-container>
    <v-row class="justify-center">
      <span>Welcome Mycat + YOU</span>
    </v-row>
    <v-row class="justify-center">
      <v-form ref="form" v-model="valid" lazy-validation>
        <slot></slot>
        <v-text-field
          v-model="formdata.name"
          :counter="10"
          :rules="[v => !!v || '请正确填写']"
          label="名字"
          required
        ></v-text-field>

        <v-text-field v-model="formdata.password" :rules="[v => !!v || '请正确填写']" label="密码" required></v-text-field>
        <v-container>
          <v-btn color="error" class="mr-4" @click="reset">重置</v-btn>
          <v-btn color="warning" :loading="querying" @click="login()">登陆</v-btn>
        </v-container>
      </v-form>
    </v-row>
  </v-container>
</template>
<script>
export default {
  props: ['saveact'],
  data: () => ({
    formdata: {
    name: '',
    password: ''
    },
    valid: true,
    querying: false
  }),

  methods: {
    validate() {
      this.$refs.form.validate()
    },
    reset() {
      this.$refs.form.reset()
      this.$refs.form.resetValidation()
    },
    async login() {
      this.valid = this.$refs.form.validate()
      if (!this.valid) {
        console.log('not valid ')
      } else {
        // 发出登陆请求
        //  await new Promise(resolve => setTimeout(resolve, 30000))
        try {
          this.querying = true
          let { data } = await this.axioscall.post('/login',
            this.$data.formdata
          )
          console.log(' login result  ' + data.retCode)
            if (data.retCode === 0) {
              window.localStorage.setItem('token', data.data.token)
              this.comutil.Token.setNewToken({
              token: data.data.token,
              expireTime: new Date(),
              refreshToken: 'testRefreshToken',
              user: data.data.user
              })
              if (this.$route.query.redirect != null) {
                this.$router.push(this.$route.query.redirect)
              } else {
                this.$router.push('/MainPage')
              }
            } else {
              this.comutil.MessageBox.show(data.data)
          }
        } catch (e) {
          console.log(' exec call error ' + e)
          this.comutil.MessageBox.show(e)
        } finally {
          this.querying = false
        }
      }
    }
  }
}
</script>

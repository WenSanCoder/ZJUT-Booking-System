前端：在终端cd src\main\resources\frontend进入src\main\resources\frontend目录

运行 

npm install(失败就用pnpm -i)

npm run dev ctrl+左键点击url打开前端页面

后端：项目直接运行即可

数据库：在application.yml中配置了云服务 也可以启用自己本地的数据库 建表语句在

db\init.sql

后端启动杀进程：
netstat -ano | findstr :8080
taskkill /F /PID <进程 PID>

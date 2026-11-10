const express = require('express');
const cors = require('cors');
const Database = require('better-sqlite3');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

const app = express();
const PORT = 3000;
const JWT_SECRET = 'hospital-equipment-management-system-jwt-secret-key';
const JWT_EXPIRES = '2h';

app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.static(path.join(__dirname, 'public')));

// ============ 数据库初始化 ============
const db = new Database(':memory:');
db.pragma('journal_mode = WAL');

db.exec(`
CREATE TABLE IF NOT EXISTS sys_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    real_name TEXT NOT NULL,
    role TEXT NOT NULL,
    dept TEXT,
    phone TEXT,
    status INTEGER NOT NULL DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS medical_device (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_name TEXT NOT NULL,
    model TEXT,
    sn_code TEXT NOT NULL UNIQUE,
    department TEXT,
    location TEXT,
    supplier TEXT,
    purchase_date TEXT,
    status INTEGER NOT NULL DEFAULT 1,
    last_inspect_date TEXT,
    next_inspect_date TEXT,
    verify_expire_date TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS repair_order (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_no TEXT NOT NULL UNIQUE,
    device_id INTEGER NOT NULL,
    reporter_id INTEGER NOT NULL,
    assignee_id INTEGER,
    description TEXT NOT NULL,
    images TEXT,
    status INTEGER NOT NULL DEFAULT 1,
    repair_remark TEXT,
    rating INTEGER,
    comment TEXT,
    finish_time DATETIME,
    close_reason TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS repair_order_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    operator_id INTEGER NOT NULL,
    action TEXT NOT NULL,
    detail TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notification (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    type TEXT NOT NULL,
    is_read INTEGER NOT NULL DEFAULT 0,
    ref_id INTEGER,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
`);

// ============ 初始化数据 ============
function initData() {
    const users = db.prepare('SELECT COUNT(*) as cnt FROM sys_user').get();
    if (users.cnt > 0) return;

    const now = new Date();
    const future = (days) => new Date(now.getTime() + days * 86400000).toISOString().split('T')[0];

    const insertUser = db.prepare('INSERT INTO sys_user (username, password, real_name, role, dept, phone) VALUES (?,?,?,?,?,?)');
    insertUser.run('admin', bcrypt.hashSync('123456'), '管理员', 'admin', '设备科', '13800000000');
    insertUser.run('engineer', bcrypt.hashSync('123456'), '李工', 'engineer', '设备科', '13800000001');
    insertUser.run('nurse', bcrypt.hashSync('123456'), '王护士', 'nurse', '内科', '13800000002');
    insertUser.run('engineer2', bcrypt.hashSync('123456'), '赵工', 'engineer', '设备科', '13800000003');
    insertUser.run('nurse2', bcrypt.hashSync('123456'), '张护士', 'nurse', '外科', '13800000004');

    const insertDevice = db.prepare(`INSERT INTO medical_device (device_name, model, sn_code, department, location, supplier, purchase_date, status, last_inspect_date, next_inspect_date, verify_expire_date) VALUES (?,?,?,?,?,?,?,?,?,?,?)`);
    const devices = [
        ['无创呼吸机','SV-300','WH20240001','内科','内科3病房','迈瑞',future(-365*2),1,future(-30),future(30),future(365*2)],
        ['心电图机','ECG-12','EC20240005','外科','外科1病房','GE医疗',future(-365),1,future(-30),future(15),future(365)],
        ['血压计','BP-200','BP20250010','内科','内科5病房','鱼跃医疗',future(-180),1,future(-30),future(45),future(365*3)],
        ['监护仪','PM-8000','PM20240012','ICU','ICU病房','迈瑞医疗',future(-365),1,future(-30),future(20),future(365)],
        ['输液泵','SP-200','SP20230003','儿科','儿科2病房','贝朗医疗',future(-365*3),1,future(-30),future(5),future(5)],
        ['雾化器','NB-150','NB20250022','呼吸科','呼吸科治疗室','欧姆龙',future(-90),1,future(-30),future(60),future(365*2)],
        ['除颤仪','Defi-Monitor','DF20220008','急诊科','急诊抢救室','飞利浦',future(-365*4),1,future(-30),future(0),future(-3)],
        ['注射泵','SP-500','SP20240045','手术室','手术室3','史密斯医疗',future(-365),1,future(-30),future(10),future(365)],
    ];
    devices.forEach(d => insertDevice.run(...d));

    console.log('  测试数据初始化完成!');
    console.log('  账号: admin / engineer / nurse  密码: 123456');
}
initData();

// ============ 中间件 ============
function auth(req, res, next) {
    const header = req.headers.authorization;
    if (!header || !header.startsWith('Bearer ')) {
        return res.status(401).json({ code: 401, message: '未登录' });
    }
    try {
        const token = header.substring(7);
        req.user = jwt.verify(token, JWT_SECRET);
        next();
    } catch {
        res.status(401).json({ code: 401, message: 'Token已过期' });
    }
}

// ============ 工具函数 ============
function generateOrderNo() {
    const d = new Date();
    const prefix = 'BX' + d.getFullYear() + String(d.getMonth()+1).padStart(2,'0') + String(d.getDate()).padStart(2,'0');
    const row = db.prepare("SELECT COUNT(*) as cnt FROM repair_order WHERE order_no LIKE ?").get(prefix + '%');
    return prefix + String((row.cnt || 0) + 1).padStart(4, '0');
}

function getStatusDesc(status) {
    const map = {1:'待受理',2:'维修中',3:'待评价',4:'已完成',5:'已关闭'};
    return map[status] || '未知';
}

function getDeviceStatusDesc(status) {
    const map = {1:'正常使用',2:'维修中',3:'停用/报废',4:'检定中'};
    return map[status] || '未知';
}

// ============ 认证接口 ============
app.post('/api/auth/login', (req, res) => {
    const { username, password } = req.body;
    const user = db.prepare('SELECT * FROM sys_user WHERE username = ?').get(username);
    if (!user || !bcrypt.compareSync(password, user.password)) {
        return res.json({ code: 1001, message: '用户名或密码错误' });
    }
    if (user.status === 0) return res.json({ code: 1002, message: '账号已禁用' });
    const token = jwt.sign(
        { userId: user.id, username: user.username, role: user.role },
        JWT_SECRET,
        { expiresIn: JWT_EXPIRES }
    );
    res.json({
        code: 200, message: 'success',
        data: {
            token, expiresIn: 7200,
            userInfo: { userId: user.id, username: user.username, realName: user.real_name, role: user.role, dept: user.dept }
        }
    });
});

// ============ 用户接口 ============
app.get('/api/user/info', auth, (req, res) => {
    const u = db.prepare('SELECT id,username,real_name,role,dept,phone FROM sys_user WHERE id=?').get(req.user.userId);
    res.json({ code: 200, data: u });
});

app.put('/api/user/password', auth, (req, res) => {
    const { oldPassword, newPassword } = req.body;
    const user = db.prepare('SELECT * FROM sys_user WHERE id=?').get(req.user.userId);
    if (!bcrypt.compareSync(oldPassword, user.password)) return res.json({ code: 1003, message: '原密码错误' });
    db.prepare('UPDATE sys_user SET password=? WHERE id=?').run(bcrypt.hashSync(newPassword), req.user.userId);
    res.json({ code: 200 });
});

// ============ 设备接口 ============
app.get('/api/device/page', auth, (req, res) => {
    const { pageNum=1, pageSize=10, keyword='', dept='', status=0 } = req.query;
    let sql = 'SELECT * FROM medical_device WHERE 1=1';
    const params = [];
    if (keyword) { sql += ' AND (device_name LIKE ? OR model LIKE ? OR sn_code LIKE ?)'; params.push(`%${keyword}%`,`%${keyword}%`,`%${keyword}%`); }
    if (dept) { sql += ' AND department = ?'; params.push(dept); }
    if (status && status != 0) { sql += ' AND status = ?'; params.push(status); }
    sql += ' ORDER BY create_time DESC LIMIT ? OFFSET ?';
    const list = db.prepare(sql).all(...params, pageSize, (pageNum-1)*pageSize);
    const countSql = sql.replace(/SELECT \*.*FROM/, 'SELECT COUNT(*) as cnt FROM').replace(/ORDER BY.*$/,'');
    const total = db.prepare(countSql).get(...params).cnt;
    res.json({ code: 200, data: { total, list } });
});

app.get('/api/device/all', auth, (req, res) => {
    const list = db.prepare('SELECT * FROM medical_device ORDER BY id').all();
    res.json({ code: 200, data: list });
});

app.get('/api/device/:id', auth, (req, res) => {
    const device = db.prepare('SELECT * FROM medical_device WHERE id=?').get(req.params.id);
    const recentOrders = db.prepare('SELECT * FROM repair_order WHERE device_id=? ORDER BY create_time DESC LIMIT 5').all(req.params.id);
    res.json({ code: 200, data: { device, recentOrders } });
});

app.post('/api/device', auth, (req, res) => {
    if (req.user.role !== 'admin') return res.json({ code: 403, message: '无权限' });
    const d = req.body;
    try {
        const info = db.prepare('INSERT INTO medical_device (device_name,model,sn_code,department,location,supplier,purchase_date,status,next_inspect_date,verify_expire_date) VALUES (?,?,?,?,?,?,?,?,?,?)')
            .run(d.deviceName, d.model, d.snCode, d.department, d.location, d.supplier, d.purchaseDate || null, d.status || 1, d.nextInspectDate || null, d.verifyExpireDate || null);
        res.json({ code: 200, data: { id: info.lastInsertRowid } });
    } catch(e) {
        res.json({ code: 2001, message: 'SN已存在' });
    }
});

app.put('/api/device', auth, (req, res) => {
    if (req.user.role !== 'admin') return res.json({ code: 403, message: '无权限' });
    const d = req.body;
    db.prepare('UPDATE medical_device SET device_name=?,model=?,sn_code=?,department=?,location=?,supplier=?,status=?,next_inspect_date=?,verify_expire_date=? WHERE id=?')
        .run(d.deviceName, d.model, d.snCode, d.department, d.location, d.supplier, d.status, d.nextInspectDate, d.verifyExpireDate, d.id);
    res.json({ code: 200 });
});

app.delete('/api/device/:id', auth, (req, res) => {
    if (req.user.role !== 'admin') return res.json({ code: 403, message: '无权限' });
    db.prepare('DELETE FROM medical_device WHERE id=?').run(req.params.id);
    res.json({ code: 200 });
});

// ============ 工单接口 ============
app.post('/api/repair-order/v2', auth, (req, res) => {
    const { deviceId, description, images=[] } = req.body;
    const device = db.prepare('SELECT * FROM medical_device WHERE id=?').get(deviceId);
    if (!device) return res.json({ code: 404, message: '设备不存在' });
    const orderNo = generateOrderNo();
    const info = db.prepare('INSERT INTO repair_order (order_no,device_id,reporter_id,description,images,status) VALUES (?,?,?,?,?,1)')
        .run(orderNo, deviceId, req.user.userId, description, images.join(','));
    db.prepare('INSERT INTO repair_order_log (order_id,operator_id,action,detail) VALUES (?,?,?,?)').run(info.lastInsertRowid, req.user.userId, 'SUBMIT', '提交报修单');
    db.prepare('UPDATE medical_device SET status=2 WHERE id=?').run(deviceId);
    res.json({ code: 200, data: { id: info.lastInsertRowid, orderNo, status: 1 } });
});

app.get('/api/repair-order/page', auth, (req, res) => {
    const { pageNum=1, pageSize=10, status=0, keyword='' } = req.query;
    let sql = 'SELECT * FROM repair_order WHERE 1=1';
    const params = [];
    if (status && status != 0) { sql += ' AND status = ?'; params.push(status); }
    if (keyword) { sql += ' AND (order_no LIKE ? OR description LIKE ?)'; params.push(`%${keyword}%`,`%${keyword}%`); }
    sql += ' ORDER BY create_time DESC LIMIT ? OFFSET ?';
    const list = db.prepare(sql).all(...params, pageSize, (pageNum-1)*pageSize);
    res.json({ code: 200, data: { total: list.length + 10, list } });
});

app.get('/api/repair-order/my', auth, (req, res) => {
    const { pageNum=1, pageSize=10, status=0 } = req.query;
    let sql = 'SELECT * FROM repair_order WHERE reporter_id = ?';
    const params = [req.user.userId];
    if (req.user.role === 'engineer') {
        sql = 'SELECT * FROM repair_order WHERE assignee_id = ?';
        params[0] = req.user.userId;
    }
    if (status && status != 0) { sql += ' AND status = ?'; params.push(status); }
    sql += ' ORDER BY create_time DESC LIMIT ? OFFSET ?';
    const list = db.prepare(sql).all(...params, pageSize, (pageNum-1)*pageSize);
    res.json({ code: 200, data: { total: list.length, list } });
});

app.get('/api/repair-order/:id', auth, (req, res) => {
    const order = db.prepare('SELECT * FROM repair_order WHERE id=?').get(req.params.id);
    if (!order) return res.json({ code: 404, message: '工单不存在' });
    const device = db.prepare('SELECT * FROM medical_device WHERE id=?').get(order.device_id);
    const reporter = db.prepare('SELECT id,real_name,phone FROM sys_user WHERE id=?').get(order.reporter_id);
    const assignee = order.assignee_id ? db.prepare('SELECT id,real_name,phone FROM sys_user WHERE id=?').get(order.assignee_id) : null;
    const logs = db.prepare('SELECT * FROM repair_order_log WHERE order_id=? ORDER BY create_time ASC').all(order.id);
    res.json({ code: 200, data: { order, device, reporter, assignee, logs } });
});

app.put('/api/repair-order/:id/accept', auth, (req, res) => {
    if (req.user.role !== 'engineer') return res.json({ code: 403, message: '无权限' });
    const order = db.prepare('SELECT * FROM repair_order WHERE id=?').get(req.params.id);
    if (!order) return res.json({ code: 404, message: '工单不存在' });
    if (order.status !== 1) return res.json({ code: 400, message: '状态不允许接单' });
    db.prepare('UPDATE repair_order SET status=2, assignee_id=? WHERE id=?').run(req.user.userId, order.id);
    db.prepare('INSERT INTO repair_order_log (order_id,operator_id,action,detail) VALUES (?,?,?,?)').run(order.id, req.user.userId, 'ACCEPT', '接单处理');
    db.prepare('INSERT INTO notification (user_id,title,content,type,ref_id) VALUES (?,?,?,?,?)')
        .run(order.reporter_id, '工单已受理', `工单 ${order.order_no} 已被工程师接单`, 'REPAIR_ACCEPTED', order.id);
    res.json({ code: 200 });
});

app.put('/api/repair-order/:id/finish', auth, (req, res) => {
    if (req.user.role !== 'engineer') return res.json({ code: 403, message: '无权限' });
    const { repairRemark, images=[] } = req.body;
    const order = db.prepare('SELECT * FROM repair_order WHERE id=?').get(req.params.id);
    if (!order) return res.json({ code: 404, message: '工单不存在' });
    if (order.status !== 2) return res.json({ code: 400, message: '状态不允许完工' });
    const now = new Date().toISOString();
    db.prepare('UPDATE repair_order SET status=3, repair_remark=?, finish_time=? WHERE id=?').run(repairRemark, now, order.id);
    db.prepare('INSERT INTO repair_order_log (order_id,operator_id,action,detail) VALUES (?,?,?,?)').run(order.id, req.user.userId, 'FINISH', `完工: ${repairRemark}`);
    db.prepare('UPDATE medical_device SET status=1 WHERE id=?').run(order.device_id);
    db.prepare('INSERT INTO notification (user_id,title,content,type,ref_id) VALUES (?,?,?,?,?)')
        .run(order.reporter_id, '工单已完工', `工单 ${order.order_no} 已完工，请及时评价`, 'REPAIR_FINISHED', order.id);
    res.json({ code: 200 });
});

app.put('/api/repair-order/:id/rate', auth, (req, res) => {
    const { rating, comment='' } = req.body;
    const order = db.prepare('SELECT * FROM repair_order WHERE id=?').get(req.params.id);
    if (!order) return res.json({ code: 404, message: '工单不存在' });
    if (order.reporter_id !== req.user.userId) return res.json({ code: 403, message: '只能评价自己的工单' });
    if (order.status !== 3) return res.json({ code: 400, message: '状态不允许评价' });
    db.prepare('UPDATE repair_order SET status=4, rating=?, comment=? WHERE id=?').run(rating, comment, order.id);
    db.prepare('INSERT INTO repair_order_log (order_id,operator_id,action,detail) VALUES (?,?,?,?)').run(order.id, req.user.userId, 'RATE', `评价: ${rating}星 ${comment}`);
    res.json({ code: 200 });
});

app.put('/api/repair-order/:id/assign', auth, (req, res) => {
    if (req.user.role !== 'admin') return res.json({ code: 403, message: '无权限' });
    const { engineerId } = req.body;
    db.prepare('UPDATE repair_order SET assignee_id=? WHERE id=?').run(engineerId, req.params.id);
    db.prepare('INSERT INTO repair_order_log (order_id,operator_id,action,detail) VALUES (?,?,?,?)').run(req.params.id, req.user.userId, 'ASSIGN', `转派给工程师`);
    res.json({ code: 200 });
});

app.put('/api/repair-order/:id/close', auth, (req, res) => {
    if (req.user.role !== 'admin') return res.json({ code: 403, message: '无权限' });
    const { closeReason } = req.body;
    db.prepare('UPDATE repair_order SET status=5, close_reason=? WHERE id=?').run(closeReason, req.params.id);
    db.prepare('INSERT INTO repair_order_log (order_id,operator_id,action,detail) VALUES (?,?,?,?)').run(req.params.id, req.user.userId, 'CLOSE', `关闭: ${closeReason}`);
    res.json({ code: 200 });
});

// ============ 通知接口 ============
app.get('/api/notification/my', auth, (req, res) => {
    const { pageNum=1, pageSize=10 } = req.query;
    const list = db.prepare('SELECT * FROM notification WHERE user_id=? ORDER BY create_time DESC LIMIT ? OFFSET ?')
        .all(req.user.userId, pageSize, (pageNum-1)*pageSize);
    res.json({ code: 200, data: { total: list.length, list } });
});

app.put('/api/notification/:id/read', auth, (req, res) => {
    db.prepare('UPDATE notification SET is_read=1 WHERE id=? AND user_id=?').run(req.params.id, req.user.userId);
    res.json({ code: 200 });
});

app.get('/api/notification/unread-count', auth, (req, res) => {
    const row = db.prepare('SELECT COUNT(*) as cnt FROM notification WHERE user_id=? AND is_read=0').get(req.user.userId);
    res.json({ code: 200, data: row.cnt });
});

// ============ 统计接口 ============
app.get('/api/stats/overview', auth, (req, res) => {
    const deviceTotal = db.prepare('SELECT COUNT(*) as cnt FROM medical_device').get().cnt;
    const deviceNormal = db.prepare('SELECT COUNT(*) as cnt FROM medical_device WHERE status=1').get().cnt;
    const repairTotal = db.prepare('SELECT COUNT(*) as cnt FROM repair_order').get().cnt;
    const repairFinished = db.prepare('SELECT COUNT(*) as cnt FROM repair_order WHERE status=4').get().cnt;
    const repairPending = db.prepare('SELECT COUNT(*) as cnt FROM repair_order WHERE status IN (1,2,3)').get().cnt;
    res.json({
        code: 200, data: {
            deviceTotal,
            deviceNormalRate: deviceTotal > 0 ? Math.round(deviceNormal/deviceTotal*100)/100 : 0,
            repairThisMonth: repairTotal,
            repairFinished,
            repairPending,
            repairFinishRate: repairTotal > 0 ? Math.round(repairFinished/repairTotal*100)/100 : 0
        }
    });
});

// ============ 巡检任务 ============
app.get('/api/inspect-task/my', auth, (req, res) => {
    const list = db.prepare('SELECT * FROM medical_device WHERE next_inspect_date <= date("now") AND status=1').all();
    res.json({ code: 200, data: { total: list.length, list } });
});

// ============ 文件上传 ============
const storage = multer.diskStorage({
    destination: (req, file, cb) => { cb(null, path.join(__dirname, 'uploads')); },
    filename: (req, file, cb) => { cb(null, Date.now() + '-' + file.originalname); }
});
const upload = multer({ storage, limits: { fileSize: 5*1024*1024 } });
if (!fs.existsSync(path.join(__dirname, 'uploads'))) fs.mkdirSync(path.join(__dirname, 'uploads'));

app.post('/api/file/upload', auth, upload.single('file'), (req, res) => {
    res.json({ code: 200, data: { url: '/uploads/' + req.file.filename, fileName: req.file.originalname } });
});
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// 首页
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, () => {
    console.log(`\n========================================`);
    console.log(` 医疗设备运维管理系统 (预览版)`);
    console.log(` API:   http://localhost:${PORT}/api/`);
    console.log(` 前端:  http://localhost:${PORT}/`);
    console.log(`========================================\n`);
});

--
-- PostgreSQL database cluster dump
--

-- Started on 2026-06-22 20:57:21

\restrict U8ZpzUdjCYE1fyQtjLT32zbRWnd5t9dv629g1uaC8xma9YVBmlGVDENAPROxJKX

SET default_transaction_read_only = off;

SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

--
-- Roles
--

CREATE ROLE postgres;
ALTER ROLE postgres WITH SUPERUSER INHERIT CREATEROLE CREATEDB LOGIN REPLICATION BYPASSRLS;

--
-- User Configurations
--








\unrestrict U8ZpzUdjCYE1fyQtjLT32zbRWnd5t9dv629g1uaC8xma9YVBmlGVDENAPROxJKX

--
-- Databases
--

--
-- Database "template1" dump
--

\connect template1

--
-- PostgreSQL database dump
--

\restrict bs6tLcwRwU3tS9kUOPGUH2m6xYo1INstDQTkNkzkX4DIQQg8mNN4TF51qhE5kbw

-- Dumped from database version 18.2
-- Dumped by pg_dump version 18.2

-- Started on 2026-06-22 20:57:21

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

-- Completed on 2026-06-22 20:57:22

--
-- PostgreSQL database dump complete
--

\unrestrict bs6tLcwRwU3tS9kUOPGUH2m6xYo1INstDQTkNkzkX4DIQQg8mNN4TF51qhE5kbw

--
-- Database "orchestria_auth_db" dump
--

--
-- PostgreSQL database dump
--

\restrict 8oSvmNp3T6PTwIGrpEFx0gaFP841XyXKC1jEi670ioduR7STk5Mn2zBaI10vgnt

-- Dumped from database version 18.2
-- Dumped by pg_dump version 18.2

-- Started on 2026-06-22 20:57:22

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5079 (class 1262 OID 41073)
-- Name: orchestria_auth_db; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE orchestria_auth_db WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'English_Indonesia.1252';


ALTER DATABASE orchestria_auth_db OWNER TO postgres;

\unrestrict 8oSvmNp3T6PTwIGrpEFx0gaFP841XyXKC1jEi670ioduR7STk5Mn2zBaI10vgnt
\connect orchestria_auth_db
\restrict 8oSvmNp3T6PTwIGrpEFx0gaFP841XyXKC1jEi670ioduR7STk5Mn2zBaI10vgnt

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 227 (class 1259 OID 66108)
-- Name: otp_challenges; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.otp_challenges (
    id character varying(36) NOT NULL,
    attempt_count integer NOT NULL,
    code_hash character varying(255) NOT NULL,
    consumed_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    max_attempts integer NOT NULL,
    purpose character varying(30) NOT NULL,
    resend_available_at timestamp(6) without time zone NOT NULL,
    resend_count integer NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    user_id bigint NOT NULL,
    CONSTRAINT otp_challenges_purpose_check CHECK (((purpose)::text = ANY ((ARRAY['LOGIN'::character varying, 'FORGOT_PASSWORD'::character varying, 'ENABLE_2FA'::character varying, 'DISABLE_2FA'::character varying])::text[])))
);


ALTER TABLE public.otp_challenges OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 66125)
-- Name: password_reset_grants; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.password_reset_grants (
    id character varying(36) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    expires_at timestamp(6) without time zone NOT NULL,
    token_hash character varying(64) NOT NULL,
    used_at timestamp(6) without time zone,
    user_id bigint NOT NULL
);


ALTER TABLE public.password_reset_grants OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 49265)
-- Name: permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.permissions (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(255),
    name character varying(100) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    active boolean NOT NULL
);


ALTER TABLE public.permissions OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 49264)
-- Name: permissions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.permissions ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.permissions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 221 (class 1259 OID 49274)
-- Name: role_permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role_permissions (
    role_id bigint NOT NULL,
    permission_id bigint NOT NULL
);


ALTER TABLE public.role_permissions OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 49282)
-- Name: roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.roles (
    id bigint NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(255),
    name character varying(50) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL
);


ALTER TABLE public.roles OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 49281)
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.roles ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 229 (class 1259 OID 66135)
-- Name: trusted_devices; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.trusted_devices (
    id character varying(36) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    device_name character varying(255),
    expires_at timestamp(6) without time zone NOT NULL,
    last_ip_address character varying(45),
    last_used_at timestamp(6) without time zone NOT NULL,
    revoked_at timestamp(6) without time zone,
    token_hash character varying(64) NOT NULL,
    user_agent character varying(500),
    user_id bigint NOT NULL
);


ALTER TABLE public.trusted_devices OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 49292)
-- Name: user_roles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_roles (
    user_id bigint NOT NULL,
    role_id bigint NOT NULL
);


ALTER TABLE public.user_roles OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 49300)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(150) NOT NULL,
    full_name character varying(150) NOT NULL,
    password character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    two_factor_enabled boolean DEFAULT false NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 49299)
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.users ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 5071 (class 0 OID 66108)
-- Dependencies: 227
-- Data for Name: otp_challenges; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.otp_challenges (id, attempt_count, code_hash, consumed_at, created_at, expires_at, max_attempts, purpose, resend_available_at, resend_count, updated_at, user_id) FROM stdin;
\.


--
-- TOC entry 5072 (class 0 OID 66125)
-- Dependencies: 228
-- Data for Name: password_reset_grants; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.password_reset_grants (id, created_at, expires_at, token_hash, used_at, user_id) FROM stdin;
\.


--
-- TOC entry 5064 (class 0 OID 49265)
-- Dependencies: 220
-- Data for Name: permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.permissions (id, created_at, description, name, updated_at, active) FROM stdin;
1	2026-06-07 01:00:34.127193	Melihat data user	auth.user.read	2026-06-07 01:00:34.127193	t
2	2026-06-07 01:00:34.253753	Mengelola user	auth.user.manage	2026-06-07 01:00:34.253753	t
3	2026-06-07 01:00:34.258753	Mengelola role dan permission	auth.role.manage	2026-06-07 01:00:34.258753	t
4	2026-06-07 01:00:34.265756	Melihat data organisasi	organization.read	2026-06-07 01:00:34.265756	t
5	2026-06-07 01:00:34.273776	Mengelola anggota, divisi, dan jabatan	organization.manage	2026-06-07 01:00:34.273776	t
6	2026-06-07 01:00:34.283839	Melihat tugas divisi	division.task.read	2026-06-07 01:00:34.283839	t
7	2026-06-07 01:00:34.289307	Mengelola tugas dan agenda divisi	division.task.manage	2026-06-07 01:00:34.289307	t
8	2026-06-07 01:00:34.295303	Membuat pengajuan operasional	request.create	2026-06-07 01:00:34.295303	t
9	2026-06-07 01:00:34.304319	Melihat pengajuan milik sendiri	request.read.own	2026-06-07 01:00:34.304319	t
10	2026-06-07 01:00:34.311302	Melihat seluruh pengajuan	request.read.all	2026-06-07 01:00:34.311302	t
11	2026-06-07 01:00:34.320304	Approval pengajuan oleh Ketua Divisi	request.approve.division	2026-06-07 01:00:34.320304	t
12	2026-06-07 01:00:34.326306	Approval pengajuan oleh Ketua PUB	request.approve.pub	2026-06-07 01:00:34.326306	t
13	2026-06-07 01:00:34.334303	Approval pengajuan oleh Pembina	request.approve.pembina	2026-06-07 01:00:34.334303	t
14	2026-06-07 01:00:34.340297	Melakukan pencairan dana	finance.disburse	2026-06-07 01:00:34.340297	t
15	2026-06-07 01:00:34.349302	Verifikasi settlement keuangan	finance.settlement.verify	2026-06-07 01:00:34.349302	t
16	2026-06-07 01:00:34.354313	Melihat laporan keuangan	finance.report.read	2026-06-07 01:00:34.354313	t
17	2026-06-07 01:00:34.360304	Mengelola arsip dokumen	archive.manage	2026-06-07 01:00:34.360304	t
18	2026-06-07 01:00:34.369313	Mengelola notifikasi	notification.manage	2026-06-07 01:00:34.369313	t
19	2026-06-07 01:00:34.37432	Melihat laporan umum	report.read	2026-06-07 01:00:34.37432	t
20	2026-06-22 20:52:06.236689	Melihat aset	asset.read	2026-06-22 20:52:06.236689	t
21	2026-06-22 20:52:06.579231	Mengelola data aset	asset.manage	2026-06-22 20:52:06.579231	t
22	2026-06-22 20:52:06.623274	Mengajukan peminjaman aset	asset.borrow.create	2026-06-22 20:52:06.623274	t
23	2026-06-22 20:52:06.640481	Melihat peminjaman sendiri	asset.borrow.read.own	2026-06-22 20:52:06.640481	t
24	2026-06-22 20:52:06.655757	Melihat semua peminjaman	asset.borrow.read.all	2026-06-22 20:52:06.655757	t
25	2026-06-22 20:52:06.69788	Merespons permohonan peminjaman	asset.borrow.approve	2026-06-22 20:52:06.69788	t
26	2026-06-22 20:52:06.719887	Menyerahkan aset	asset.borrow.handover	2026-06-22 20:52:06.719887	t
27	2026-06-22 20:52:06.739958	Memverifikasi pengembalian aset	asset.return.verify	2026-06-22 20:52:06.739958	t
28	2026-06-22 20:52:06.763027	Mengelola kondisi aset	asset.condition.manage	2026-06-22 20:52:06.763027	t
29	2026-06-22 20:52:06.790844	Melihat jadwal piket kebersihan	cleanliness.schedule.read	2026-06-22 20:52:06.790844	t
30	2026-06-22 20:52:06.805374	Mengelola jadwal piket kebersihan	cleanliness.schedule.manage	2026-06-22 20:52:06.805374	t
31	2026-06-22 20:52:06.829508	Mengisi presensi piket	cleanliness.attendance.create	2026-06-22 20:52:06.829508	t
32	2026-06-22 20:52:06.856516	Melihat presensi piket	cleanliness.attendance.read	2026-06-22 20:52:06.856516	t
33	2026-06-22 20:52:06.875215	Mengelola poin reward piket	cleanliness.point.manage	2026-06-22 20:52:06.875215	t
34	2026-06-22 20:52:06.895771	Mengelola poin pelanggaran piket	cleanliness.violation.manage	2026-06-22 20:52:06.895771	t
35	2026-06-22 20:52:06.921064	Melihat laporan piket kebersihan	cleanliness.report.read	2026-06-22 20:52:06.921064	t
36	2026-06-22 20:52:06.932067	Melihat jadwal aktivitas bahasa Inggris	english.activity.read	2026-06-22 20:52:06.932067	t
37	2026-06-22 20:52:06.951081	Mengelola aktivitas bahasa Inggris	english.activity.manage	2026-06-22 20:52:06.951081	t
38	2026-06-22 20:52:06.962613	Membuat setoran bahasa Inggris	english.deposit.create	2026-06-22 20:52:06.962613	t
39	2026-06-22 20:52:06.976616	Melihat setoran bahasa Inggris sendiri	english.deposit.read.own	2026-06-22 20:52:06.976616	t
40	2026-06-22 20:52:07.001736	Melihat seluruh setoran bahasa Inggris	english.deposit.read.all	2026-06-22 20:52:07.001736	t
41	2026-06-22 20:52:07.030255	Memverifikasi setoran bahasa Inggris	english.deposit.verify	2026-06-22 20:52:07.030255	t
42	2026-06-22 20:52:07.072808	Melihat laporan aktivitas bahasa Inggris	english.report.read	2026-06-22 20:52:07.072808	t
43	2026-06-22 20:52:07.103821	Melihat konten publik admin	public.content.read	2026-06-22 20:52:07.103821	t
44	2026-06-22 20:52:07.128467	Mengelola program, fasilitas, dan testimoni	public.content.manage	2026-06-22 20:52:07.128467	t
45	2026-06-22 20:52:07.18627	Mengelola profil publik organisasi	public.organization.manage	2026-06-22 20:52:07.18627	t
46	2026-06-22 20:52:07.228391	Mengelola publikasi kegiatan	public.activity.manage	2026-06-22 20:52:07.228391	t
47	2026-06-22 20:52:07.281891	Mengelola metadata media publik	public.media.manage	2026-06-22 20:52:07.281891	t
48	2026-06-22 20:52:07.300909	Mengirim notifikasi manual	notification.send	2026-06-22 20:52:07.300909	t
49	2026-06-22 20:52:07.327981	Melihat notifikasi	notification.read	2026-06-22 20:52:07.327981	t
50	2026-06-22 20:52:07.368153	Mencoba ulang notifikasi gagal	notification.retry	2026-06-22 20:52:07.368153	t
51	2026-06-22 20:52:07.411571	Mengekspor laporan	report.export	2026-06-22 20:52:07.411571	t
52	2026-06-22 20:52:07.436851	Mengimpor data laporan	report.import	2026-06-22 20:52:07.436851	t
53	2026-06-22 20:52:07.478416	Melihat log penjadwalan	scheduler.log.read	2026-06-22 20:52:07.478416	t
\.


--
-- TOC entry 5065 (class 0 OID 49274)
-- Dependencies: 221
-- Data for Name: role_permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.role_permissions (role_id, permission_id) FROM stdin;
2	14
2	1
2	3
2	10
2	15
2	19
2	4
2	13
2	17
2	5
2	7
2	16
2	2
2	12
2	11
2	9
2	18
2	6
2	8
3	4
3	10
3	6
3	13
3	16
3	19
4	12
4	17
4	4
4	19
4	6
4	7
4	10
4	16
4	5
4	8
5	7
5	4
5	8
5	11
5	6
5	9
6	5
6	19
6	17
6	4
7	15
7	4
7	19
7	16
7	14
7	10
8	4
8	16
8	19
1	4
1	6
1	9
1	8
2	29
2	40
2	48
2	22
2	49
2	36
2	47
2	37
2	51
2	33
2	24
2	44
2	32
2	35
2	21
2	27
2	46
2	50
2	31
2	38
2	41
2	28
2	26
2	39
2	43
2	20
2	53
2	23
2	45
2	52
2	25
2	34
2	30
2	42
11	45
11	46
11	47
11	43
11	44
10	34
10	37
10	32
10	38
10	42
10	40
10	39
10	41
10	30
10	33
10	36
10	29
10	31
10	35
10	43
3	51
3	40
3	29
3	35
3	24
3	48
3	49
3	36
3	20
3	32
3	42
3	53
4	28
4	42
4	47
4	25
4	45
4	48
4	44
4	43
4	21
4	24
4	36
4	32
4	46
4	26
4	27
4	51
4	35
4	40
4	49
4	20
4	53
4	29
5	23
5	20
5	22
6	51
6	45
6	48
6	52
6	53
6	49
6	50
6	43
7	51
8	51
9	4
9	21
9	20
9	26
9	25
9	24
9	27
9	28
1	31
1	38
1	23
1	39
1	20
1	29
1	22
1	36
\.


--
-- TOC entry 5067 (class 0 OID 49282)
-- Dependencies: 223
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.roles (id, active, created_at, description, name, updated_at) FROM stdin;
1	t	2026-05-30 00:25:11.221013	Role default untuk Anggota	ANGGOTA	2026-05-30 00:25:11.221013
2	t	2026-06-07 01:00:34.393837	Akses penuh sistem	SUPER_ADMIN	2026-06-07 01:00:34.393837
3	t	2026-06-07 01:00:34.430852	Pembina organisasi	PEMBINA	2026-06-07 01:00:34.430852
4	t	2026-06-07 01:00:34.437835	Ketua PUB	KETUA_PUB	2026-06-07 01:00:34.437835
5	t	2026-06-07 01:00:34.443836	Ketua Divisi	KETUA_DIVISI	2026-06-07 01:00:34.443836
6	t	2026-06-07 01:00:34.452832	Sekretaris organisasi	SEKRETARIS	2026-06-07 01:00:34.452832
7	t	2026-06-07 01:00:34.459834	Bendahara internal	BENDAHARA_INTERNAL	2026-06-07 01:00:34.459834
8	t	2026-06-07 01:00:34.468856	Bendahara eksternal	BENDAHARA_EKSTERNAL	2026-06-07 01:00:34.469837
9	t	2026-06-22 20:52:07.845411	Pemeriksa dan pengelola operasional aset	CHECKER	2026-06-22 20:52:07.845411
10	t	2026-06-22 20:52:07.93203	Koordinator piket	KOORDINATOR	2026-06-22 20:52:07.933031
11	t	2026-06-22 20:52:07.989076	Pengelola publikasi dan media organisasi	HUMAS	2026-06-22 20:52:07.989076
\.


--
-- TOC entry 5073 (class 0 OID 66135)
-- Dependencies: 229
-- Data for Name: trusted_devices; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.trusted_devices (id, created_at, device_name, expires_at, last_ip_address, last_used_at, revoked_at, token_hash, user_agent, user_id) FROM stdin;
\.


--
-- TOC entry 5068 (class 0 OID 49292)
-- Dependencies: 224
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_roles (user_id, role_id) FROM stdin;
1	1
2	1
2	2
3	2
4	2
5	3
6	4
7	1
8	6
9	7
10	8
11	1
12	1
13	5
14	1
15	5
16	1
17	1
18	1
19	1
20	1
21	1
22	1
23	1
24	1
25	5
26	5
27	1
28	1
29	1
30	5
31	1
32	1
33	1
34	5
35	1
36	5
37	1
38	1
39	1
40	1
41	5
42	1
43	1
44	1
9	1
10	1
\.


--
-- TOC entry 5070 (class 0 OID 49300)
-- Dependencies: 226
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, active, created_at, email, full_name, password, updated_at, two_factor_enabled) FROM stdin;
1	t	2026-05-30 00:25:11.53491	pangeran@gmail.com	Pangeran Valerensco	$2a$10$1RgMPJU7cZT7xyzV1hT.LesNfu1RX0dirnvEGVjJ6DM50j5mDc/bq	2026-05-30 00:25:11.53491	f
2	t	2026-06-07 01:32:13.864388	superadmin@orchestria.local	Super Admin Orchestria	$2a$10$0PC0UZn42.WRmc2mrMjZu.L0EpF1SPo9hQAuRzAbV8JuwhyfAasqm	2026-06-07 01:32:13.864388	f
3	t	2026-06-18 01:56:14.383177	admin@example.com	Super Admin Orchestria	$2a$10$UA5cdbAH/hKrBw4Qx3nv0.U0.W69tBca35SO1G1atkeuoQ98E9eSW	2026-06-18 01:56:14.383177	f
4	t	2026-06-18 02:06:58.8072	admin@orchestria.local	Super Admin Orchestria	$2a$10$fIbIma2/JV2n/XPuYIoMS.VmtBDGz9PallUklBuCA..we7Iy/2pye	2026-06-18 02:06:58.8072	f
5	t	2026-06-19 14:08:45.500098	abdul.hafiz.tanjung@orchestria.local	Abdul Hafiz Tanjung	$2a$10$kbQIDnytPDDVzLMBtOybReMRssDUr.d/cy8uZm/N0pOMnJfOKnDNq	2026-06-19 14:08:45.500098	f
6	t	2026-06-19 14:08:47.182779	pangeran.valerensco.rivaldi.hutabarat@orchestria.local	Pangeran Valerensco Rivaldi Hutabarat	$2a$10$0pGaV2QpTZTJof8EgqoN4.ItGthBNLgmWCCtZpsfMU20IqXZH/K3.	2026-06-19 14:08:47.182779	f
7	t	2026-06-19 14:08:48.148305	ikram.fuadi.rambe@orchestria.local	Ikram Fuadi Rambe	$2a$10$7QYUlE5YHn5S0zggN5hKtemdEc/oDHfRNGuIpCfM6ZPXqrDTq4oGS	2026-06-19 14:08:48.148305	f
8	t	2026-06-19 14:08:49.427469	khalisha.ulfa.marsha@orchestria.local	Khalisha Ulfa Marsha	$2a$10$buDFNa68VMJoXTbVUSKZgOPLkXIN6zJSFdtTYojH9jGpoFh6/KSEy	2026-06-19 14:08:49.427469	f
9	t	2026-06-19 14:08:50.404435	andini.siti.nuriyanti@orchestria.local	Andini Siti Nuriyanti	$2a$10$tQsuUIwrFK/AgLUCTnZvLuodbDuiV5ATSP4AWjpvuDllioKEcHi66	2026-06-19 14:08:50.404435	f
10	t	2026-06-19 14:08:51.241602	sri.rahayu.lestari@orchestria.local	Sri Rahayu Lestari	$2a$10$P/Gk5HPUJuIHn6V2uH1vaOYknwcw040z1WHZCXBSvo/VftUugfPfy	2026-06-19 14:08:51.241602	f
11	t	2026-06-19 14:08:52.059122	dedy.darmawan.simanjuntak@orchestria.local	Dedy Darmawan Simanjuntak	$2a$10$u37hR/1cHmsxQnq4SEN3TOTKPrO1qEGEzRyxIunJlM4KiWbll/ImK	2026-06-19 14:08:52.059122	f
12	t	2026-06-19 14:08:52.856342	firman.suherman@orchestria.local	Firman Suherman	$2a$10$HtbIAAqdN117Jhvpk1HENetG.8oc.hIdrr5QjKFbmFkIf7o939zyK	2026-06-19 14:08:52.856342	f
13	t	2026-06-19 14:08:53.838963	rickhy.ramadhan@orchestria.local	Rickhy Ramadhan	$2a$10$KT94vqvUL0/RN.OTRLzBNOvQKjgKXESQrSevp5BUv0E4teW4WBjyC	2026-06-19 14:08:53.838963	f
14	t	2026-06-19 14:08:54.996772	raysha.fauziyah.andani@orchestria.local	Raysha Fauziyah Andani	$2a$10$in4AyblhL.drFnk7Z2ErP.SwRmaK1.9W8FA.T02.NHBVA3l35R9P.	2026-06-19 14:08:54.996772	f
15	t	2026-06-19 14:08:55.723384	yudistira.syahputra@orchestria.local	Yudistira Syahputra	$2a$10$zdyX6Oz/zlyQGTEF9d5G4ewk.PoITnObA6jx7J1T69nCcZtYvFmju	2026-06-19 14:08:55.723384	f
16	t	2026-06-19 14:08:56.40192	taufik.rahman.tanjung@orchestria.local	Taufik Rahman Tanjung	$2a$10$7DZlXCx0AJzNLDSYyQFd2OK3MCqnKhSb791BE8dJGGNPQgiyVQcDG	2026-06-19 14:08:56.40192	f
17	t	2026-06-19 14:08:57.397964	izhar.harahap@orchestria.local	Izhar Harahap	$2a$10$jbZ3GxPAXmvpjvPXIiA8g.tqnQQ7sA2s/ewdEucrKHQFnN0GPAe6e	2026-06-19 14:08:57.397964	f
18	t	2026-06-19 14:08:58.012787	m.faiq.emil.fuadi@orchestria.local	M Faiq Emil Fuadi	$2a$10$YbozXsIZs7tA0cbx/9.FyOE3.oLW24rVz5nDCapwPHvzZ9lOHyXoW	2026-06-19 14:08:58.012787	f
19	t	2026-06-19 14:08:58.619399	alif.rifki.pratama@orchestria.local	Alif Rifki Pratama	$2a$10$9c.j3E/HIfV7X6NRxW7Cp.DcmX9ldPBj7Qr/ZLHdsHdilArH4KuaW	2026-06-19 14:08:58.619399	f
20	t	2026-06-19 14:08:59.321748	dhea.firmasari@orchestria.local	Dhea Firmasari	$2a$10$G/C/edc.tjJHzZSVqpeIiuz/gldnqf1GCQgbyaHTZD6ng.2z109Fi	2026-06-19 14:08:59.321748	f
21	t	2026-06-19 14:08:59.908577	nabila.monica@orchestria.local	Nabila Monica	$2a$10$Qv7XnP3fGn696kItmoOQwuYtNDfnkpQ6QDYtDYIcIfVdeo3rhvsXy	2026-06-19 14:08:59.908577	f
22	t	2026-06-19 14:09:00.711145	ines.karlina@orchestria.local	Ines Karlina	$2a$10$//AAuiQ16HuiC4eL.B5woe6vVSgtLWjuD3CscqSxIqGqupkhV3eKe	2026-06-19 14:09:00.711145	f
23	t	2026-06-19 14:09:01.248919	miftahul.jannah.harahap@orchestria.local	Miftahul Jannah Harahap	$2a$10$AVYGD7ijBKHB1sajgmKPeeVVKNATH92Py6x1RHF.NA/35ljUkOmxW	2026-06-19 14:09:01.248919	f
24	t	2026-06-19 14:09:01.864707	dea.afrilia@orchestria.local	Dea Afrilia	$2a$10$urGP0EnrG2B/wwYgbfJk6esl7wfPAOWN7F1laVxAYycCsVzIUwAOy	2026-06-19 14:09:01.864707	f
25	t	2026-06-19 14:09:02.66932	zaky.setiawan@orchestria.local	Zaky Setiawan	$2a$10$Ltdr5r5R5jGowQ2aOHgVhOqMNlyQfRBozPZH6kPIl1At0J1d5WhpS	2026-06-19 14:09:02.66932	f
26	t	2026-06-19 14:09:03.236622	fajar.sidik@orchestria.local	Fajar Sidik	$2a$10$6mIP0EEgY8tugazbdQAN7u6vsqWMqbYiE8faN4U02Yfh27ab9OUKq	2026-06-19 14:09:03.236622	f
27	t	2026-06-19 14:09:03.828249	alfarizi@orchestria.local	Alfarizi	$2a$10$V8uu8x2XH7Fklv5czixD6.EjLEeePjjvwwMz6lv3HHzZPDcty/UUG	2026-06-19 14:09:03.828249	f
28	t	2026-06-19 14:09:04.533698	apriliyanti@orchestria.local	Apriliyanti	$2a$10$ZlLHaS9ZxOylR5v9dtW7fusLvvfr6L.KSkYrNoFWIpTiDC7xPHb5C	2026-06-19 14:09:04.533698	f
29	t	2026-06-19 14:09:05.040914	sri.muthian@orchestria.local	Sri Muthian	$2a$10$Drp5ALen/v4N9auTdUbgf.wBHN5O1Oo06vD/4SJhQeMXG2D9uUMMu	2026-06-19 14:09:05.040914	f
30	t	2026-06-19 14:09:05.544633	muhammad.farid@orchestria.local	Muhammad Farid	$2a$10$wAYu.zkjk5KxlRNCmKurMeO0DyrdKtEEyZpYpy2qZh1uZBP17Y4Cq	2026-06-19 14:09:05.544633	f
31	t	2026-06-19 14:09:06.222889	azhar.farizi@orchestria.local	Azhar Farizi	$2a$10$59LaZ8QspsJd46AYdH/TqeHnsFOFs5rQU2BrrMmCf7qLtIDTZEFw2	2026-06-19 14:09:06.222889	f
32	t	2026-06-19 14:09:07.134815	ewi.lestari.harahap@orchestria.local	Ewi Lestari Harahap	$2a$10$WtH9g3BpXZtIvTJ6Yr4COeUDXescl1HaKPhmlQEwaLwsqPe2OxaAS	2026-06-19 14:09:07.134815	f
33	t	2026-06-19 14:09:07.604119	yusri.hasanah@orchestria.local	Yusri Hasanah	$2a$10$gZ3pc2CRmwBEJTgTXALKrOq4CJxYufytOp3X2ZicJIb1t18wpQC1S	2026-06-19 14:09:07.604119	f
34	t	2026-06-19 14:09:08.061565	ahmad.zaki.hosammido@orchestria.local	Ahmad Zaki Hosammido	$2a$10$..FqmVc9aNzPGpVFJDLXfuIPlJgPQocq63j2lkxwOw1to7e4cJY1y	2026-06-19 14:09:08.061565	f
35	t	2026-06-19 14:09:08.652449	ade.dermawan@orchestria.local	Ade Dermawan	$2a$10$s1dysp8rs5HT44jN/lFTF.5eH2aNdbCMkHq/J0N.gYJ/.Ppd/rJDi	2026-06-19 14:09:08.652449	f
36	t	2026-06-19 14:09:09.106388	m.saroni@orchestria.local	M Saroni	$2a$10$8BoU0vVmlPqh18FmGf.ldOiVb8WkVLhO3GDpoVYTnJWbZNi2Ygbum	2026-06-19 14:09:09.106388	f
37	t	2026-06-19 14:09:09.529443	ali.sahroji@orchestria.local	Ali Sahroji	$2a$10$TW7j/rzlDKVZ7NXXLTT.Q.AaAm3n1JbAWhnn8BSswA8/jrRtsZhle	2026-06-19 14:09:09.529443	f
38	t	2026-06-19 14:09:09.980276	sri.muthia.ningrum@orchestria.local	Sri Muthia Ningrum	$2a$10$nrxxvSQL6rnPiRHaA0ZNT.FS8bcau4O6RurZyZhSMLXuLhAQr1OFy	2026-06-19 14:09:09.980276	f
39	t	2026-06-19 14:09:10.471673	galang.ponco.maulana@orchestria.local	Galang Ponco Maulana	$2a$10$fPFq8l4woZJhiYbfVWSSe.Q6UAgMD6eDRABbnV5sjha01VSUEbeo2	2026-06-19 14:09:10.471673	f
40	t	2026-06-19 14:09:11.046643	padellan.riski@orchestria.local	Padellan Riski	$2a$10$uchEU1d2vwYewuDUmOwysu3qPx6aAIvvyfelRO9bIbAEqR9PS68F.	2026-06-19 14:09:11.046643	f
41	t	2026-06-19 14:09:11.594433	m.haikal@orchestria.local	M Haikal	$2a$10$UXJihT55HIMEAWXPkEerXuNOXaoJWYpiV7XolX/eo3.V6/Wr1ApIq	2026-06-19 14:09:11.594433	f
42	t	2026-06-19 14:09:12.012806	alfa.rizi@orchestria.local	Alfa Rizi	$2a$10$KtHlxN1WBkuBz49vgGi1LuY1k.1ZMtqvSIuIcc12h2iAPdIQCXBMG	2026-06-19 14:09:12.012806	f
43	t	2026-06-19 14:09:12.493581	raja.tegar.albaihaqi@orchestria.local	Raja Tegar Albaihaqi	$2a$10$nBBsKG2N2BFkycMZJd0z9.jo3QfuwRNtC8YxLlX6b85eR83Oudl3u	2026-06-19 14:09:12.493581	f
44	t	2026-06-19 14:09:12.984043	ulil.arsyad.ramadhan@orchestria.local	Ulil Arsyad Ramadhan	$2a$10$plgBIOrML1TvVS9q88ikqOwtw93dbAluEueyArDS3NsjCRJucPqai	2026-06-19 14:09:12.984043	f
\.


--
-- TOC entry 5080 (class 0 OID 0)
-- Dependencies: 219
-- Name: permissions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.permissions_id_seq', 53, true);


--
-- TOC entry 5081 (class 0 OID 0)
-- Dependencies: 222
-- Name: roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.roles_id_seq', 11, true);


--
-- TOC entry 5082 (class 0 OID 0)
-- Dependencies: 225
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 44, true);


--
-- TOC entry 4905 (class 2606 OID 66124)
-- Name: otp_challenges otp_challenges_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.otp_challenges
    ADD CONSTRAINT otp_challenges_pkey PRIMARY KEY (id);


--
-- TOC entry 4907 (class 2606 OID 66134)
-- Name: password_reset_grants password_reset_grants_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_grants
    ADD CONSTRAINT password_reset_grants_pkey PRIMARY KEY (id);


--
-- TOC entry 4889 (class 2606 OID 49273)
-- Name: permissions permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (id);


--
-- TOC entry 4893 (class 2606 OID 49280)
-- Name: role_permissions role_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_pkey PRIMARY KEY (role_id, permission_id);


--
-- TOC entry 4895 (class 2606 OID 49291)
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- TOC entry 4911 (class 2606 OID 66147)
-- Name: trusted_devices trusted_devices_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trusted_devices
    ADD CONSTRAINT trusted_devices_pkey PRIMARY KEY (id);


--
-- TOC entry 4901 (class 2606 OID 49319)
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- TOC entry 4909 (class 2606 OID 66150)
-- Name: password_reset_grants uke15qi31wwa7n18vr0dkym7sg3; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_grants
    ADD CONSTRAINT uke15qi31wwa7n18vr0dkym7sg3 UNIQUE (token_hash);


--
-- TOC entry 4897 (class 2606 OID 49317)
-- Name: roles ukofx66keruapi6vyqpv6f2or37; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT ukofx66keruapi6vyqpv6f2or37 UNIQUE (name);


--
-- TOC entry 4891 (class 2606 OID 49315)
-- Name: permissions ukpnvtwliis6p05pn6i3ndjrqt2; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT ukpnvtwliis6p05pn6i3ndjrqt2 UNIQUE (name);


--
-- TOC entry 4899 (class 2606 OID 49298)
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);


--
-- TOC entry 4903 (class 2606 OID 49313)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 4912 (class 2606 OID 49320)
-- Name: role_permissions fkegdk29eiy7mdtefy5c7eirr6e; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT fkegdk29eiy7mdtefy5c7eirr6e FOREIGN KEY (permission_id) REFERENCES public.permissions(id);


--
-- TOC entry 4914 (class 2606 OID 49330)
-- Name: user_roles fkh8ciramu9cc9q3qcqiv4ue8a6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkh8ciramu9cc9q3qcqiv4ue8a6 FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- TOC entry 4915 (class 2606 OID 49335)
-- Name: user_roles fkhfh9dx7w3ubf1co1vdev94g3f; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT fkhfh9dx7w3ubf1co1vdev94g3f FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- TOC entry 4913 (class 2606 OID 49325)
-- Name: role_permissions fkn5fotdgk8d1xvo8nav9uv3muc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT fkn5fotdgk8d1xvo8nav9uv3muc FOREIGN KEY (role_id) REFERENCES public.roles(id);


-- Completed on 2026-06-22 20:57:23

--
-- PostgreSQL database dump complete
--

\unrestrict 8oSvmNp3T6PTwIGrpEFx0gaFP841XyXKC1jEi670ioduR7STk5Mn2zBaI10vgnt

--
-- Database "orchestria_finance_db" dump
--

--
-- PostgreSQL database dump
--

\restrict fXYRT6liBCYQkIbrwdqXBFRGjs9yqpOnD46KCaqW4l2f5C7arjyuavPFJcXTSEZ

-- Dumped from database version 18.2
-- Dumped by pg_dump version 18.2

-- Started on 2026-06-22 20:57:23

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5018 (class 1262 OID 65648)
-- Name: orchestria_finance_db; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE orchestria_finance_db WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'English_Indonesia.1252';


ALTER DATABASE orchestria_finance_db OWNER TO postgres;

\unrestrict fXYRT6liBCYQkIbrwdqXBFRGjs9yqpOnD46KCaqW4l2f5C7arjyuavPFJcXTSEZ
\connect orchestria_finance_db
\restrict fXYRT6liBCYQkIbrwdqXBFRGjs9yqpOnD46KCaqW4l2f5C7arjyuavPFJcXTSEZ

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 220 (class 1259 OID 65650)
-- Name: fund_disbursements; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fund_disbursements (
    id bigint NOT NULL,
    active boolean NOT NULL,
    amount numeric(18,2) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    disbursed_at timestamp(6) without time zone NOT NULL,
    disbursed_by_email character varying(150) NOT NULL,
    division_id bigint NOT NULL,
    division_name character varying(150) NOT NULL,
    fund_request_id bigint NOT NULL,
    method character varying(50) NOT NULL,
    note text,
    proof_url character varying(500),
    receiver_name character varying(150) NOT NULL,
    receiver_note text,
    request_title character varying(150) NOT NULL,
    requester_name character varying(150) NOT NULL,
    status character varying(50) NOT NULL,
    updated_at timestamp(6) without time zone,
    request_sync_error character varying(500),
    request_synced_at timestamp(6) without time zone,
    request_sync_status character varying(30) DEFAULT 'PENDING'::character varying NOT NULL,
    request_sync_attempts integer DEFAULT 0 NOT NULL,
    CONSTRAINT fund_disbursements_method_check CHECK (((method)::text = ANY ((ARRAY['CASH'::character varying, 'BANK_TRANSFER'::character varying, 'E_WALLET'::character varying])::text[]))),
    CONSTRAINT fund_disbursements_status_check CHECK (((status)::text = ANY ((ARRAY['DISBURSED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.fund_disbursements OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 65649)
-- Name: fund_disbursements_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.fund_disbursements ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.fund_disbursements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 5012 (class 0 OID 65650)
-- Dependencies: 220
-- Data for Name: fund_disbursements; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fund_disbursements (id, active, amount, created_at, disbursed_at, disbursed_by_email, division_id, division_name, fund_request_id, method, note, proof_url, receiver_name, receiver_note, request_title, requester_name, status, updated_at, request_sync_error, request_synced_at, request_sync_status, request_sync_attempts) FROM stdin;
1	t	250000.00	2026-06-16 02:26:53.180219	2026-06-16 02:26:53.180219	superadmin@orchestria.local	1	Divisi Kesejahteraan	1	CASH	Pencairan dana konsumsi rapat	https://example.com/proof.jpg	Pangeran Valerensco Rivaldi Hutabarat	Dana diterima langsung oleh pengaju	Pengajuan Konsumsi Rapat Divisi	Pangeran Valerensco Rivaldi Hutabarat	DISBURSED	2026-06-16 02:26:53.180219	\N	\N	SYNCED	0
2	t	8000.00	2026-06-19 15:52:22.500288	2026-06-19 15:52:22.500288	andini.siti.nuriyanti@orchestria.local	1	Divisi Pendidikan dan Pelatihan	4	BANK_TRANSFER	pencairan core flow	\N	Izhar Harahap	02102012012010010	tes flow	Izhar Harahap	DISBURSED	2026-06-19 15:52:23.142226	\N	2026-06-19 15:52:23.117674	SYNCED	1
\.


--
-- TOC entry 5019 (class 0 OID 0)
-- Dependencies: 219
-- Name: fund_disbursements_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.fund_disbursements_id_seq', 2, true);


--
-- TOC entry 4861 (class 2606 OID 65672)
-- Name: fund_disbursements fund_disbursements_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fund_disbursements
    ADD CONSTRAINT fund_disbursements_pkey PRIMARY KEY (id);


--
-- TOC entry 4863 (class 2606 OID 65701)
-- Name: fund_disbursements uk_fund_disbursement_request; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fund_disbursements
    ADD CONSTRAINT uk_fund_disbursement_request UNIQUE (fund_request_id);


-- Completed on 2026-06-22 20:57:23

--
-- PostgreSQL database dump complete
--

\unrestrict fXYRT6liBCYQkIbrwdqXBFRGjs9yqpOnD46KCaqW4l2f5C7arjyuavPFJcXTSEZ

--
-- Database "orchestria_notification_db" dump
--

--
-- PostgreSQL database dump
--

\restrict 1ile5SdtPmiukrBre4Uv54tKdUtlskuCIuDsoCUX6eYgPItrnFzKu55eYoDO5PA

-- Dumped from database version 18.2
-- Dumped by pg_dump version 18.2

-- Started on 2026-06-22 20:57:24

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5039 (class 1262 OID 66333)
-- Name: orchestria_notification_db; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE orchestria_notification_db WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'English_Indonesia.1252';


ALTER DATABASE orchestria_notification_db OWNER TO postgres;

\unrestrict 1ile5SdtPmiukrBre4Uv54tKdUtlskuCIuDsoCUX6eYgPItrnFzKu55eYoDO5PA
\connect orchestria_notification_db
\restrict 1ile5SdtPmiukrBre4Uv54tKdUtlskuCIuDsoCUX6eYgPItrnFzKu55eYoDO5PA

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 219 (class 1259 OID 66334)
-- Name: notification_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notification_logs (
    id character varying(255) NOT NULL,
    attempt_count integer NOT NULL,
    bcc_recipients text,
    body text NOT NULL,
    cc_recipients text,
    created_at timestamp(6) without time zone NOT NULL,
    created_by_email character varying(255) NOT NULL,
    html boolean NOT NULL,
    last_attempt_at timestamp(6) without time zone,
    last_error text,
    next_retry_at timestamp(6) without time zone,
    sent_at timestamp(6) without time zone,
    status character varying(255) NOT NULL,
    subject character varying(255) NOT NULL,
    to_recipients text,
    CONSTRAINT notification_logs_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'SENT'::character varying, 'FAILED'::character varying])::text[])))
);


ALTER TABLE public.notification_logs OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 66350)
-- Name: report_export_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.report_export_logs (
    id character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    created_by_email character varying(255),
    error_message text,
    file_size bigint,
    filename character varying(255),
    finished_at timestamp(6) without time zone,
    record_count integer,
    report_type character varying(255) NOT NULL,
    requested_by_email character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    CONSTRAINT report_export_logs_report_type_check CHECK (((report_type)::text = ANY ((ARRAY['WEEKLY_REQUEST_REPORT'::character varying, 'FUND_REQUEST'::character varying])::text[]))),
    CONSTRAINT report_export_logs_status_check CHECK (((status)::text = ANY ((ARRAY['PROCESSING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying])::text[])))
);


ALTER TABLE public.report_export_logs OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 66364)
-- Name: report_subscribers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.report_subscribers (
    id character varying(255) NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    report_type character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    CONSTRAINT report_subscribers_report_type_check CHECK (((report_type)::text = ANY ((ARRAY['WEEKLY_REQUEST_REPORT'::character varying, 'FUND_REQUEST'::character varying])::text[])))
);


ALTER TABLE public.report_subscribers OWNER TO postgres;

--
-- TOC entry 222 (class 1259 OID 66378)
-- Name: scheduled_job_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.scheduled_job_logs (
    id character varying(255) NOT NULL,
    error_message text,
    finished_at timestamp(6) without time zone,
    job_name character varying(255) NOT NULL,
    message text,
    started_at timestamp(6) without time zone NOT NULL,
    status character varying(255) NOT NULL,
    trigger_type character varying(255) NOT NULL,
    CONSTRAINT scheduled_job_logs_status_check CHECK (((status)::text = ANY ((ARRAY['SUCCESS'::character varying, 'FAILED'::character varying, 'SKIPPED'::character varying])::text[]))),
    CONSTRAINT scheduled_job_logs_trigger_type_check CHECK (((trigger_type)::text = ANY ((ARRAY['FIXED_RATE'::character varying, 'FIXED_DELAY'::character varying, 'CRON'::character varying, 'MANUAL'::character varying])::text[])))
);


ALTER TABLE public.scheduled_job_logs OWNER TO postgres;

--
-- TOC entry 5030 (class 0 OID 66334)
-- Dependencies: 219
-- Data for Name: notification_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notification_logs (id, attempt_count, bcc_recipients, body, cc_recipients, created_at, created_by_email, html, last_attempt_at, last_error, next_retry_at, sent_at, status, subject, to_recipients) FROM stdin;
\.


--
-- TOC entry 5031 (class 0 OID 66350)
-- Dependencies: 220
-- Data for Name: report_export_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.report_export_logs (id, created_at, created_by_email, error_message, file_size, filename, finished_at, record_count, report_type, requested_by_email, status) FROM stdin;
\.


--
-- TOC entry 5032 (class 0 OID 66364)
-- Dependencies: 221
-- Data for Name: report_subscribers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.report_subscribers (id, active, created_at, email, name, report_type, updated_at) FROM stdin;
\.


--
-- TOC entry 5033 (class 0 OID 66378)
-- Dependencies: 222
-- Data for Name: scheduled_job_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.scheduled_job_logs (id, error_message, finished_at, job_name, message, started_at, status, trigger_type) FROM stdin;
90d3e401-0218-4148-aceb-390e7d6e899e	\N	2026-06-22 20:55:54.324959	Health Ping	Job executed successfully	2026-06-22 20:55:54.269317	SUCCESS	FIXED_RATE
fcba07bb-61b1-4a98-ad54-6fd797e6efd0	\N	2026-06-22 20:55:55.030902	Retry Failed Email	tidak ada notifikasi retry due	2026-06-22 20:55:54.557446	SUCCESS	FIXED_DELAY
\.


--
-- TOC entry 4874 (class 2606 OID 66349)
-- Name: notification_logs notification_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notification_logs
    ADD CONSTRAINT notification_logs_pkey PRIMARY KEY (id);


--
-- TOC entry 4876 (class 2606 OID 66363)
-- Name: report_export_logs report_export_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.report_export_logs
    ADD CONSTRAINT report_export_logs_pkey PRIMARY KEY (id);


--
-- TOC entry 4878 (class 2606 OID 66377)
-- Name: report_subscribers report_subscribers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.report_subscribers
    ADD CONSTRAINT report_subscribers_pkey PRIMARY KEY (id);


--
-- TOC entry 4882 (class 2606 OID 66391)
-- Name: scheduled_job_logs scheduled_job_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.scheduled_job_logs
    ADD CONSTRAINT scheduled_job_logs_pkey PRIMARY KEY (id);


--
-- TOC entry 4880 (class 2606 OID 66393)
-- Name: report_subscribers ukaftmxskb2b675psehtixa0n8c; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.report_subscribers
    ADD CONSTRAINT ukaftmxskb2b675psehtixa0n8c UNIQUE (email, report_type);


-- Completed on 2026-06-22 20:57:24

--
-- PostgreSQL database dump complete
--

\unrestrict 1ile5SdtPmiukrBre4Uv54tKdUtlskuCIuDsoCUX6eYgPItrnFzKu55eYoDO5PA

--
-- Database "orchestria_organization_db" dump
--

--
-- PostgreSQL database dump
--

\restrict Kwe9HLPpu5Kxnc3n4SAVuN87TpNiLGFixNfVsynm9Ag6j1oceA4ksd5b5GEURPB

-- Dumped from database version 18.2
-- Dumped by pg_dump version 18.2

-- Started on 2026-06-22 20:57:24

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5188 (class 1262 OID 57459)
-- Name: orchestria_organization_db; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE orchestria_organization_db WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'English_Indonesia.1252';


ALTER DATABASE orchestria_organization_db OWNER TO postgres;

\unrestrict Kwe9HLPpu5Kxnc3n4SAVuN87TpNiLGFixNfVsynm9Ag6j1oceA4ksd5b5GEURPB
\connect orchestria_organization_db
\restrict Kwe9HLPpu5Kxnc3n4SAVuN87TpNiLGFixNfVsynm9Ag6j1oceA4ksd5b5GEURPB

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 234 (class 1259 OID 66089)
-- Name: archive_documents; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.archive_documents (
    id bigint NOT NULL,
    category character varying(50) NOT NULL,
    content_type character varying(200) NOT NULL,
    deleted boolean NOT NULL,
    deleted_at timestamp(6) without time zone,
    deleted_by_email character varying(500),
    description text,
    lock_version bigint,
    original_file_name character varying(500) NOT NULL,
    size_bytes bigint NOT NULL,
    storage_reference character varying(1000) NOT NULL,
    stored_file_name character varying(500) NOT NULL,
    title character varying(500) NOT NULL,
    uploaded_at timestamp(6) without time zone NOT NULL,
    uploaded_by_email character varying(500) NOT NULL,
    uploaded_by_name character varying(500),
    CONSTRAINT archive_documents_category_check CHECK (((category)::text = ANY ((ARRAY['SURAT_MASUK'::character varying, 'SURAT_KELUAR'::character varying, 'PROPOSAL'::character varying, 'LAPORAN'::character varying, 'NOTULEN'::character varying, 'DOKUMENTASI'::character varying, 'LAINNYA'::character varying])::text[])))
);


ALTER TABLE public.archive_documents OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 66088)
-- Name: archive_documents_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.archive_documents ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.archive_documents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 235 (class 1259 OID 66151)
-- Name: asset_borrowings; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.asset_borrowings (
    id character varying(255) NOT NULL,
    active boolean NOT NULL,
    actual_return_date date,
    approved_at timestamp(6) without time zone,
    approved_by_email character varying(255),
    borrow_date date NOT NULL,
    borrower_auth_user_id bigint,
    borrower_email character varying(255) NOT NULL,
    borrower_member_id bigint NOT NULL,
    borrower_name character varying(255) NOT NULL,
    cancellation_reason character varying(255),
    condition_after character varying(255),
    condition_before character varying(255),
    created_at timestamp(6) without time zone,
    expected_return_date date NOT NULL,
    handed_over_at timestamp(6) without time zone,
    handed_over_by_email character varying(255),
    handover_proof_url character varying(255),
    note character varying(255),
    purpose character varying(1000) NOT NULL,
    rejection_reason character varying(255),
    return_proof_url character varying(255),
    return_requested_at timestamp(6) without time zone,
    return_verified_at timestamp(6) without time zone,
    return_verified_by_email character varying(255),
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    asset_id character varying(255) NOT NULL,
    CONSTRAINT asset_borrowings_condition_after_check CHECK (((condition_after)::text = ANY ((ARRAY['GOOD'::character varying, 'MINOR_DAMAGE'::character varying, 'DAMAGED'::character varying, 'UNKNOWN'::character varying])::text[]))),
    CONSTRAINT asset_borrowings_condition_before_check CHECK (((condition_before)::text = ANY ((ARRAY['GOOD'::character varying, 'MINOR_DAMAGE'::character varying, 'DAMAGED'::character varying, 'UNKNOWN'::character varying])::text[]))),
    CONSTRAINT asset_borrowings_status_check CHECK (((status)::text = ANY ((ARRAY['REQUESTED'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'BORROWED'::character varying, 'RETURN_REQUESTED'::character varying, 'RETURN_VERIFIED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.asset_borrowings OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 66171)
-- Name: asset_condition_histories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.asset_condition_histories (
    id character varying(255) NOT NULL,
    checked_at timestamp(6) without time zone NOT NULL,
    checked_by_email character varying(255) NOT NULL,
    created_at timestamp(6) without time zone,
    new_condition character varying(255) NOT NULL,
    new_status character varying(255) NOT NULL,
    note character varying(255),
    old_condition character varying(255),
    old_status character varying(255),
    asset_id character varying(255) NOT NULL,
    borrowing_id character varying(255),
    CONSTRAINT asset_condition_histories_new_condition_check CHECK (((new_condition)::text = ANY ((ARRAY['GOOD'::character varying, 'MINOR_DAMAGE'::character varying, 'DAMAGED'::character varying, 'UNKNOWN'::character varying])::text[]))),
    CONSTRAINT asset_condition_histories_new_status_check CHECK (((new_status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'RESERVED'::character varying, 'BORROWED'::character varying, 'MAINTENANCE'::character varying, 'LOST'::character varying, 'INACTIVE'::character varying])::text[]))),
    CONSTRAINT asset_condition_histories_old_condition_check CHECK (((old_condition)::text = ANY ((ARRAY['GOOD'::character varying, 'MINOR_DAMAGE'::character varying, 'DAMAGED'::character varying, 'UNKNOWN'::character varying])::text[]))),
    CONSTRAINT asset_condition_histories_old_status_check CHECK (((old_status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'RESERVED'::character varying, 'BORROWED'::character varying, 'MAINTENANCE'::character varying, 'LOST'::character varying, 'INACTIVE'::character varying])::text[])))
);


ALTER TABLE public.asset_condition_histories OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 66188)
-- Name: assets; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.assets (
    id character varying(255) NOT NULL,
    active boolean NOT NULL,
    asset_code character varying(50) NOT NULL,
    asset_name character varying(150) NOT NULL,
    category character varying(100) NOT NULL,
    created_at timestamp(6) without time zone,
    current_condition character varying(255) NOT NULL,
    current_status character varying(255) NOT NULL,
    description character varying(1000),
    image_url character varying(500),
    location character varying(200),
    responsible_member_id bigint,
    updated_at timestamp(6) without time zone,
    CONSTRAINT assets_current_condition_check CHECK (((current_condition)::text = ANY ((ARRAY['GOOD'::character varying, 'MINOR_DAMAGE'::character varying, 'DAMAGED'::character varying, 'UNKNOWN'::character varying])::text[]))),
    CONSTRAINT assets_current_status_check CHECK (((current_status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'RESERVED'::character varying, 'BORROWED'::character varying, 'MAINTENANCE'::character varying, 'LOST'::character varying, 'INACTIVE'::character varying])::text[])))
);


ALTER TABLE public.assets OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 66204)
-- Name: cleanliness_assignments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cleanliness_assignments (
    id character varying(255) NOT NULL,
    active boolean NOT NULL,
    attendance_note text,
    attendance_status character varying(255) NOT NULL,
    attended_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone,
    evidence_url character varying(255),
    member_email character varying(255) NOT NULL,
    member_id bigint NOT NULL,
    member_name character varying(255) NOT NULL,
    recorded_by_email character varying(255),
    updated_at timestamp(6) without time zone,
    schedule_id character varying(255) NOT NULL,
    CONSTRAINT cleanliness_assignments_attendance_status_check CHECK (((attendance_status)::text = ANY ((ARRAY['PENDING'::character varying, 'PRESENT'::character varying, 'ABSENT'::character varying, 'EXCUSED'::character varying])::text[])))
);


ALTER TABLE public.cleanliness_assignments OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 66219)
-- Name: cleanliness_point_records; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cleanliness_point_records (
    id character varying(255) NOT NULL,
    active boolean NOT NULL,
    member_id bigint NOT NULL,
    member_name character varying(255) NOT NULL,
    point_value integer NOT NULL,
    reason text NOT NULL,
    recorded_at timestamp(6) without time zone NOT NULL,
    recorded_by_email character varying(255) NOT NULL,
    schedule_id character varying(255),
    type character varying(255) NOT NULL,
    CONSTRAINT cleanliness_point_records_type_check CHECK (((type)::text = ANY ((ARRAY['REWARD'::character varying, 'VIOLATION'::character varying])::text[])))
);


ALTER TABLE public.cleanliness_point_records OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 66236)
-- Name: cleanliness_schedules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cleanliness_schedules (
    id character varying(255) NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone,
    created_by_email character varying(255) NOT NULL,
    description text,
    duty_date date NOT NULL,
    end_time time(0) without time zone NOT NULL,
    location character varying(255) NOT NULL,
    start_time time(0) without time zone NOT NULL,
    status character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    CONSTRAINT cleanliness_schedules_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PUBLISHED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.cleanliness_schedules OWNER TO postgres;

--
-- TOC entry 220 (class 1259 OID 57461)
-- Name: division_task_evidences; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.division_task_evidences (
    id bigint NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(1000),
    external_link character varying(500),
    file_url character varying(500),
    title character varying(150) NOT NULL,
    type character varying(30) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    task_id bigint NOT NULL,
    submitted_by_member_id bigint,
    CONSTRAINT division_task_evidences_type_check CHECK (((type)::text = ANY ((ARRAY['PHOTO'::character varying, 'DOCUMENT'::character varying, 'LINK'::character varying, 'NOTE'::character varying])::text[])))
);


ALTER TABLE public.division_task_evidences OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 57460)
-- Name: division_task_evidences_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.division_task_evidences ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.division_task_evidences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 222 (class 1259 OID 57477)
-- Name: division_tasks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.division_tasks (
    id bigint NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(1000),
    due_date date,
    priority character varying(30) NOT NULL,
    status character varying(30) NOT NULL,
    title character varying(150) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    assigned_member_id bigint,
    division_id bigint NOT NULL,
    CONSTRAINT division_tasks_priority_check CHECK (((priority)::text = ANY ((ARRAY['LOW'::character varying, 'MEDIUM'::character varying, 'HIGH'::character varying])::text[]))),
    CONSTRAINT division_tasks_status_check CHECK (((status)::text = ANY ((ARRAY['TODO'::character varying, 'IN_PROGRESS'::character varying, 'DONE'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.division_tasks OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 57476)
-- Name: division_tasks_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.division_tasks ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.division_tasks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 224 (class 1259 OID 57495)
-- Name: divisions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.divisions (
    id bigint NOT NULL,
    active boolean NOT NULL,
    code character varying(50) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(500),
    name character varying(100) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    display_order integer NOT NULL,
    public_visible boolean NOT NULL
);


ALTER TABLE public.divisions OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 57494)
-- Name: divisions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.divisions ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.divisions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 241 (class 1259 OID 66253)
-- Name: english_activities; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.english_activities (
    id character varying(255) NOT NULL,
    active boolean NOT NULL,
    activity_date date NOT NULL,
    created_at timestamp(6) without time zone,
    created_by_email character varying(255) NOT NULL,
    description text,
    end_time time(0) without time zone NOT NULL,
    start_time time(0) without time zone NOT NULL,
    status character varying(255) NOT NULL,
    title character varying(255) NOT NULL,
    topic character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    CONSTRAINT english_activities_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PUBLISHED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.english_activities OWNER TO postgres;

--
-- TOC entry 242 (class 1259 OID 66270)
-- Name: english_deposits; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.english_deposits (
    id character varying(255) NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone,
    evidence_url character varying(1000) NOT NULL,
    member_email character varying(255) NOT NULL,
    member_id bigint NOT NULL,
    member_name character varying(255) NOT NULL,
    score numeric(5,2),
    status character varying(255) NOT NULL,
    submission_note text,
    submitted_at timestamp(6) without time zone NOT NULL,
    topic character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    verification_note text,
    verified_at timestamp(6) without time zone,
    verified_by_email character varying(255),
    activity_id character varying(255) NOT NULL,
    CONSTRAINT english_deposits_status_check CHECK (((status)::text = ANY ((ARRAY['SUBMITTED'::character varying, 'VERIFIED'::character varying, 'REJECTED'::character varying, 'MISSED'::character varying])::text[])))
);


ALTER TABLE public.english_deposits OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 57509)
-- Name: member_assignments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.member_assignments (
    id bigint NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    status character varying(30) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    division_id bigint NOT NULL,
    member_id bigint NOT NULL,
    period_id bigint NOT NULL,
    position_id bigint NOT NULL,
    CONSTRAINT member_assignments_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])))
);


ALTER TABLE public.member_assignments OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 57508)
-- Name: member_assignments_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.member_assignments ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.member_assignments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 228 (class 1259 OID 57525)
-- Name: members; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.members (
    id bigint NOT NULL,
    active boolean NOT NULL,
    auth_user_id bigint,
    cohort character varying(100),
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(150) NOT NULL,
    full_name character varying(150) NOT NULL,
    phone_number character varying(30),
    status character varying(30) NOT NULL,
    student_number character varying(50),
    updated_at timestamp(6) without time zone NOT NULL,
    campus_class character varying(100),
    display_order integer NOT NULL,
    major character varying(100),
    profile_photo_url character varying(500),
    public_visible boolean NOT NULL,
    CONSTRAINT members_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'ALUMNI'::character varying])::text[])))
);


ALTER TABLE public.members OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 57524)
-- Name: members_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.members ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.members_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 230 (class 1259 OID 57541)
-- Name: organization_periods; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.organization_periods (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    end_date date,
    active boolean CONSTRAINT organization_periods_is_active_not_null NOT NULL,
    name character varying(100) NOT NULL,
    start_date date,
    updated_at timestamp(6) without time zone NOT NULL,
    current_period boolean NOT NULL,
    public_visible boolean NOT NULL
);


ALTER TABLE public.organization_periods OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 57540)
-- Name: organization_periods_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.organization_periods ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.organization_periods_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 232 (class 1259 OID 57552)
-- Name: positions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.positions (
    id bigint NOT NULL,
    active boolean NOT NULL,
    code character varying(50) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description character varying(500),
    level_order integer NOT NULL,
    name character varying(100) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    public_visible boolean NOT NULL
);


ALTER TABLE public.positions OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 57551)
-- Name: positions_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.positions ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.positions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 243 (class 1259 OID 66288)
-- Name: public_content_entries; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.public_content_entries (
    id character varying(255) NOT NULL,
    active boolean NOT NULL,
    author_name character varying(255),
    author_role character varying(255),
    body text,
    category character varying(100),
    content_type character varying(255) NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    created_by_email character varying(100),
    display_order integer NOT NULL,
    event_date date,
    link_url character varying(2000),
    media_url character varying(2000),
    publication_status character varying(255) NOT NULL,
    published_at timestamp(6) without time zone,
    status_label character varying(100),
    subtitle character varying(255),
    title character varying(255),
    updated_at timestamp(6) without time zone NOT NULL,
    updated_by_email character varying(100),
    CONSTRAINT public_content_entries_content_type_check CHECK (((content_type)::text = ANY ((ARRAY['HERO'::character varying, 'ABOUT'::character varying, 'VISION'::character varying, 'MISSION'::character varying, 'PROGRAM'::character varying, 'FACILITY'::character varying, 'TESTIMONIAL'::character varying, 'ACTIVITY'::character varying, 'MEDIA'::character varying])::text[]))),
    CONSTRAINT public_content_entries_publication_status_check CHECK (((publication_status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PUBLISHED'::character varying, 'ARCHIVED'::character varying])::text[])))
);


ALTER TABLE public.public_content_entries OWNER TO postgres;

--
-- TOC entry 5173 (class 0 OID 66089)
-- Dependencies: 234
-- Data for Name: archive_documents; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.archive_documents (id, category, content_type, deleted, deleted_at, deleted_by_email, description, lock_version, original_file_name, size_bytes, storage_reference, stored_file_name, title, uploaded_at, uploaded_by_email, uploaded_by_name) FROM stdin;
1	LAPORAN	application/pdf	f	\N	\N	ini daftar penempatan magang	0	Daftar_Penempatan_Magang_PUB_20250-2026.pdf	479584	81937f7f-96de-4d76-a79d-8e4302c12f8d.pdf	81937f7f-96de-4d76-a79d-8e4302c12f8d.pdf	Daftar Penempatan Magang	2026-06-21 03:46:45.973912	pangeran.valerensco.rivaldi.hutabarat@orchestria.local	\N
2	PROPOSAL	application/pdf	f	\N	\N	\N	0	PROPOSAL KEGIATAN MAKRAB HIMATIF 2026.pdf	550805	be49fe19-4d76-447d-ad71-f53de32ff8de.pdf	be49fe19-4d76-447d-ad71-f53de32ff8de.pdf	Proposal Makrab	2026-06-21 03:47:38.461835	pangeran.valerensco.rivaldi.hutabarat@orchestria.local	\N
\.


--
-- TOC entry 5174 (class 0 OID 66151)
-- Dependencies: 235
-- Data for Name: asset_borrowings; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.asset_borrowings (id, active, actual_return_date, approved_at, approved_by_email, borrow_date, borrower_auth_user_id, borrower_email, borrower_member_id, borrower_name, cancellation_reason, condition_after, condition_before, created_at, expected_return_date, handed_over_at, handed_over_by_email, handover_proof_url, note, purpose, rejection_reason, return_proof_url, return_requested_at, return_verified_at, return_verified_by_email, status, updated_at, asset_id) FROM stdin;
\.


--
-- TOC entry 5175 (class 0 OID 66171)
-- Dependencies: 236
-- Data for Name: asset_condition_histories; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.asset_condition_histories (id, checked_at, checked_by_email, created_at, new_condition, new_status, note, old_condition, old_status, asset_id, borrowing_id) FROM stdin;
\.


--
-- TOC entry 5176 (class 0 OID 66188)
-- Dependencies: 237
-- Data for Name: assets; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.assets (id, active, asset_code, asset_name, category, created_at, current_condition, current_status, description, image_url, location, responsible_member_id, updated_at) FROM stdin;
\.


--
-- TOC entry 5177 (class 0 OID 66204)
-- Dependencies: 238
-- Data for Name: cleanliness_assignments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cleanliness_assignments (id, active, attendance_note, attendance_status, attended_at, created_at, evidence_url, member_email, member_id, member_name, recorded_by_email, updated_at, schedule_id) FROM stdin;
\.


--
-- TOC entry 5178 (class 0 OID 66219)
-- Dependencies: 239
-- Data for Name: cleanliness_point_records; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cleanliness_point_records (id, active, member_id, member_name, point_value, reason, recorded_at, recorded_by_email, schedule_id, type) FROM stdin;
\.


--
-- TOC entry 5179 (class 0 OID 66236)
-- Dependencies: 240
-- Data for Name: cleanliness_schedules; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cleanliness_schedules (id, active, created_at, created_by_email, description, duty_date, end_time, location, start_time, status, title, updated_at) FROM stdin;
\.


--
-- TOC entry 5159 (class 0 OID 57461)
-- Dependencies: 220
-- Data for Name: division_task_evidences; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.division_task_evidences (id, active, created_at, description, external_link, file_url, title, type, updated_at, task_id, submitted_by_member_id) FROM stdin;
1	t	2026-06-09 00:57:24.152961	Dokumentasi hasil koordinasi penyusunan jadwal pelatihan.	https://drive.google.com/example	\N	Link dokumentasi rapat Divdik	LINK	2026-06-09 00:57:24.152961	1	\N
\.


--
-- TOC entry 5161 (class 0 OID 57477)
-- Dependencies: 222
-- Data for Name: division_tasks; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.division_tasks (id, active, created_at, description, due_date, priority, status, title, updated_at, assigned_member_id, division_id) FROM stdin;
1	t	2026-06-09 00:41:52.207622	Menyusun jadwal materi, instruktur, dan pembagian kelas untuk pelatihan Java.	2026-06-10	HIGH	DONE	Susun jadwal pelatihan Java minggu ini	2026-06-09 00:42:45.239169	1	1
\.


--
-- TOC entry 5163 (class 0 OID 57495)
-- Dependencies: 224
-- Data for Name: divisions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.divisions (id, active, code, created_at, description, name, updated_at, display_order, public_visible) FROM stdin;
1	t	DIVDIK	2026-06-07 06:45:27.110282	Mengelola pelatihan, kurikulum, instruktur, dan pembelajaran PUB.	Divisi Pendidikan dan Pelatihan	2026-06-07 06:45:27.110282	1	t
2	t	HUMAS	2026-06-08 01:31:41.400479	Mengelola publikasi, dokumentasi, media sosial, web, dan citra PUB.	Hubungan Masyarakat	2026-06-08 01:31:41.400479	2	t
3	t	KESEJAHTERAAN	2026-06-08 01:31:41.410482	Mengelola kebutuhan konsumsi, menu, dan kesejahteraan mahasiswa PUB.	Divisi Kesejahteraan	2026-06-08 01:31:41.410482	3	t
4	t	KEBERSIHAN	2026-06-08 01:31:41.42348	Mengelola jadwal piket, area kebersihan, dan kedisiplinan kebersihan lingkungan.	Divisi Kebersihan	2026-06-08 01:31:41.42348	4	t
5	t	BAHASA_INGGRIS	2026-06-08 01:31:41.431481	Mengelola setoran vocabulary, latihan bahasa Inggris, dan evaluasi pembelajaran bahasa.	Divisi Bahasa Inggris	2026-06-08 01:31:41.431481	5	t
6	t	KEROHANIAN	2026-06-08 01:31:41.443497	Mengelola pengajian, setoran bacaan Al-Qur'an, dan pembinaan ibadah.	Divisi Kerohanian	2026-06-08 01:31:41.443497	6	t
7	t	KEASRAMAAN	2026-06-08 01:31:41.460738	Mengelola kedisiplinan asrama, keberadaan mahasiswa, dan administrasi izin asrama.	Divisi Keasramaan	2026-06-08 01:31:41.460738	7	t
8	t	KESEHATAN	2026-06-08 01:31:41.473731	Mengelola perhatian kesehatan, pendataan sakit, dan kebutuhan kesehatan mahasiswa PUB.	Divisi Kesehatan	2026-06-08 01:31:41.473731	8	t
9	t	PPMB	2026-06-08 01:31:41.48876	Mengelola proses sosialisasi, seleksi, survei, dan penerimaan mahasiswa baru PUB.	Divisi PPMB	2026-06-08 01:31:41.48876	9	t
10	t	ASET	2026-06-08 01:31:41.51373	Mengelola pendataan aset, peminjaman laptop, dan pemeriksaan kondisi barang.	Divisi Aset	2026-06-08 01:31:41.51373	10	t
11	t	PENGURUS_INTI	2026-06-19 15:06:41.529967	Struktur inti PUB di luar pembagian divisi operasional.	Pengurus Inti	2026-06-19 15:06:41.529967	0	t
\.


--
-- TOC entry 5180 (class 0 OID 66253)
-- Dependencies: 241
-- Data for Name: english_activities; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.english_activities (id, active, activity_date, created_at, created_by_email, description, end_time, start_time, status, title, topic, updated_at) FROM stdin;
\.


--
-- TOC entry 5181 (class 0 OID 66270)
-- Dependencies: 242
-- Data for Name: english_deposits; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.english_deposits (id, active, created_at, evidence_url, member_email, member_id, member_name, score, status, submission_note, submitted_at, topic, updated_at, verification_note, verified_at, verified_by_email, activity_id) FROM stdin;
\.


--
-- TOC entry 5165 (class 0 OID 57509)
-- Dependencies: 226
-- Data for Name: member_assignments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.member_assignments (id, active, created_at, status, updated_at, division_id, member_id, period_id, position_id) FROM stdin;
1	t	2026-06-09 00:04:08.894578	ACTIVE	2026-06-09 00:04:08.894578	1	1	1	2
2	t	2026-06-19 01:15:09.225934	ACTIVE	2026-06-19 01:15:09.225934	1	1	1	8
3	t	2026-06-19 01:36:35.506335	ACTIVE	2026-06-19 01:36:35.506335	1	2	1	8
4	t	2026-06-19 15:06:42.090357	ACTIVE	2026-06-19 15:06:42.090357	11	3	1	3
5	t	2026-06-19 15:06:42.264485	ACTIVE	2026-06-19 15:06:42.264485	11	4	1	4
6	t	2026-06-19 15:06:42.447086	ACTIVE	2026-06-19 15:06:42.447086	11	5	1	1
7	t	2026-06-19 15:06:42.772342	ACTIVE	2026-06-19 15:06:42.773352	11	6	1	5
8	t	2026-06-19 15:06:42.97948	ACTIVE	2026-06-19 15:06:42.97948	11	7	1	6
9	t	2026-06-19 15:06:43.114052	ACTIVE	2026-06-19 15:06:43.114052	11	8	1	7
10	t	2026-06-19 15:06:43.265164	ACTIVE	2026-06-19 15:06:43.265164	1	9	1	2
11	t	2026-06-19 15:06:43.437728	ACTIVE	2026-06-19 15:06:43.437728	1	10	1	2
12	t	2026-06-19 15:06:43.516295	ACTIVE	2026-06-19 15:06:43.516295	1	11	1	8
13	t	2026-06-19 15:06:43.582855	ACTIVE	2026-06-19 15:06:43.582855	1	12	1	5
14	t	2026-06-19 15:06:43.726409	ACTIVE	2026-06-19 15:06:43.726409	1	13	1	12
15	t	2026-06-19 15:06:43.814971	ACTIVE	2026-06-19 15:06:43.814971	1	14	1	12
16	t	2026-06-19 15:06:43.978107	ACTIVE	2026-06-19 15:06:43.978107	1	15	1	12
17	t	2026-06-19 15:06:44.124671	ACTIVE	2026-06-19 15:06:44.124671	1	16	1	12
18	t	2026-06-19 15:06:44.20723	ACTIVE	2026-06-19 15:06:44.20723	1	17	1	12
19	t	2026-06-19 15:06:44.459969	ACTIVE	2026-06-19 15:06:44.459969	1	18	1	12
20	t	2026-06-19 15:06:44.559534	ACTIVE	2026-06-19 15:06:44.559534	1	19	1	12
21	t	2026-06-19 15:06:44.854705	ACTIVE	2026-06-19 15:06:44.854705	1	20	1	12
22	t	2026-06-19 15:06:45.022817	ACTIVE	2026-06-19 15:06:45.022817	1	21	1	12
23	t	2026-06-19 15:06:45.130378	ACTIVE	2026-06-19 15:06:45.130378	1	22	1	12
24	t	2026-06-19 15:06:45.246956	ACTIVE	2026-06-19 15:06:45.246956	5	23	1	8
25	t	2026-06-19 15:06:45.410073	ACTIVE	2026-06-19 15:06:45.410073	5	13	1	12
26	t	2026-06-19 15:06:45.77236	ACTIVE	2026-06-19 15:06:45.77236	5	14	1	12
27	t	2026-06-19 15:06:45.900931	ACTIVE	2026-06-19 15:06:45.900931	5	24	1	12
28	t	2026-06-19 15:06:45.968513	ACTIVE	2026-06-19 15:06:45.968513	5	16	1	12
29	t	2026-06-19 15:06:46.032508	ACTIVE	2026-06-19 15:06:46.032508	5	17	1	12
30	t	2026-06-19 15:06:46.126077	ACTIVE	2026-06-19 15:06:46.126077	5	25	1	12
31	t	2026-06-19 15:06:46.202636	ACTIVE	2026-06-19 15:06:46.202636	5	18	1	12
32	t	2026-06-19 15:06:46.292201	ACTIVE	2026-06-19 15:06:46.292201	5	26	1	12
33	t	2026-06-19 15:06:46.384776	ACTIVE	2026-06-19 15:06:46.384776	5	12	1	12
34	t	2026-06-19 15:06:46.431776	ACTIVE	2026-06-19 15:06:46.431776	5	8	1	12
35	t	2026-06-19 15:06:46.465328	ACTIVE	2026-06-19 15:06:46.465328	5	22	1	12
36	t	2026-06-19 15:06:46.542348	ACTIVE	2026-06-19 15:06:46.542348	5	27	1	12
37	t	2026-06-19 15:06:46.59089	ACTIVE	2026-06-19 15:06:46.59089	7	28	1	8
38	t	2026-06-19 15:06:46.643934	ACTIVE	2026-06-19 15:06:46.643934	7	29	1	12
39	t	2026-06-19 15:06:46.777072	ACTIVE	2026-06-19 15:06:46.777072	7	30	1	12
40	t	2026-06-19 15:06:46.851103	ACTIVE	2026-06-19 15:06:46.851103	7	31	1	12
41	t	2026-06-19 15:06:46.918653	ACTIVE	2026-06-19 15:06:46.918653	6	32	1	8
42	t	2026-06-19 15:06:47.003237	ACTIVE	2026-06-19 15:06:47.003237	6	33	1	12
43	t	2026-06-19 15:06:47.100799	ACTIVE	2026-06-19 15:06:47.100799	6	19	1	12
44	t	2026-06-19 15:06:47.144822	ACTIVE	2026-06-19 15:06:47.144822	6	7	1	12
45	t	2026-06-19 15:06:47.245944	ACTIVE	2026-06-19 15:06:47.245944	3	34	1	8
46	t	2026-06-19 15:06:47.328491	ACTIVE	2026-06-19 15:06:47.328491	3	35	1	12
47	t	2026-06-19 15:06:47.462624	ACTIVE	2026-06-19 15:06:47.462624	3	36	1	12
48	t	2026-06-19 15:06:47.525628	ACTIVE	2026-06-19 15:06:47.526625	3	37	1	12
49	t	2026-06-19 15:06:47.554654	ACTIVE	2026-06-19 15:06:47.554654	8	24	1	8
50	t	2026-06-19 15:06:47.593211	ACTIVE	2026-06-19 15:06:47.593211	8	38	1	12
51	t	2026-06-19 15:06:47.675775	ACTIVE	2026-06-19 15:06:47.675775	8	21	1	12
52	t	2026-06-19 15:06:47.788343	ACTIVE	2026-06-19 15:06:47.788343	8	26	1	12
53	t	2026-06-19 15:06:47.843376	ACTIVE	2026-06-19 15:06:47.843376	4	39	1	8
54	t	2026-06-19 15:06:47.915928	ACTIVE	2026-06-19 15:06:47.915928	4	40	1	12
55	t	2026-06-19 15:06:48.121098	ACTIVE	2026-06-19 15:06:48.121098	4	41	1	12
56	t	2026-06-19 15:06:48.212661	ACTIVE	2026-06-19 15:06:48.212661	4	42	1	12
57	t	2026-06-19 15:06:48.289229	ACTIVE	2026-06-19 15:06:48.289229	2	13	1	8
58	t	2026-06-19 15:06:48.489382	ACTIVE	2026-06-19 15:06:48.489382	2	16	1	12
59	t	2026-06-19 15:06:48.567941	ACTIVE	2026-06-19 15:06:48.567941	2	22	1	12
60	t	2026-06-19 15:06:48.642944	ACTIVE	2026-06-19 15:06:48.642944	2	19	1	12
\.


--
-- TOC entry 5167 (class 0 OID 57525)
-- Dependencies: 228
-- Data for Name: members; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.members (id, active, auth_user_id, cohort, created_at, email, full_name, phone_number, status, student_number, updated_at, campus_class, display_order, major, profile_photo_url, public_visible) FROM stdin;
1	t	1	PUB 2025	2026-06-08 17:32:27.831536	admin@orchestria.local	Super Admin Orchestria	081234567890	ACTIVE	2025001	2026-06-19 01:15:08.996835	TI PUB 2025	1	Teknik Informatika	\N	t
2	t	2	\N	2026-06-19 01:36:35.092287	superadmin@orchestria.local	Super Admin Orchestria	\N	ACTIVE	\N	2026-06-19 01:36:35.092287	\N	1	\N	\N	t
3	t	\N	PUB 2025	2026-06-19 15:06:41.910215	abdul.hafiz.tanjung@orchestria.local	Abdul Hafiz Tanjung	\N	ACTIVE	\N	2026-06-19 16:54:33.05956	\N	99	\N	\N	t
4	t	\N	PUB 2025	2026-06-19 15:06:42.199913	pangeran.valerensco.rivaldi.hutabarat@orchestria.local	Pangeran Valerensco Rivaldi Hutabarat	\N	ACTIVE	\N	2026-06-19 16:54:33.255656	\N	99	\N	\N	t
5	t	\N	PUB 2025	2026-06-19 15:06:42.333478	ikram.fuadi.rambe@orchestria.local	Ikram Fuadi Rambe	\N	ACTIVE	\N	2026-06-19 16:54:33.362211	\N	99	\N	\N	t
6	t	\N	PUB 2025	2026-06-19 15:06:42.593202	khalisha.ulfa.marsha@orchestria.local	Khalisha Ulfa Marsha	\N	ACTIVE	\N	2026-06-19 16:54:33.522315	\N	99	\N	\N	t
7	t	\N	PUB 2025	2026-06-19 15:06:42.875916	andini.siti.nuriyanti@orchestria.local	Andini Siti Nuriyanti	\N	ACTIVE	\N	2026-06-19 16:54:33.606867	\N	99	\N	\N	t
8	t	\N	PUB 2025	2026-06-19 15:06:43.050493	sri.rahayu.lestari@orchestria.local	Sri Rahayu Lestari	\N	ACTIVE	\N	2026-06-19 16:54:33.776414	\N	99	\N	\N	t
9	t	\N	PUB 2025	2026-06-19 15:06:43.155603	dedy.darmawan.simanjuntak@orchestria.local	Dedy Darmawan Simanjuntak	\N	ACTIVE	\N	2026-06-19 16:54:33.898516	\N	99	\N	\N	t
10	t	\N	PUB 2025	2026-06-19 15:06:43.341181	firman.suherman@orchestria.local	Firman Suherman	\N	ACTIVE	\N	2026-06-19 16:54:34.135621	\N	99	\N	\N	t
11	t	13	PUB 2025	2026-06-19 15:06:43.481287	rickhy.ramadhan@orchestria.local	Rickhy Ramadhan	\N	ACTIVE	\N	2026-06-19 16:54:34.277176	\N	99	\N	\N	t
12	t	\N	PUB 2025	2026-06-19 15:06:43.548305	raysha.fauziyah.andani@orchestria.local	Raysha Fauziyah Andani	\N	ACTIVE	\N	2026-06-19 16:54:34.343732	\N	99	\N	\N	t
13	t	\N	PUB 2025	2026-06-19 15:06:43.658414	yudistira.syahputra@orchestria.local	Yudistira Syahputra	\N	ACTIVE	\N	2026-06-19 16:54:34.420286	\N	99	\N	\N	t
14	t	\N	PUB 2025	2026-06-19 15:06:43.761969	taufik.rahman.tanjung@orchestria.local	Taufik Rahman Tanjung	\N	ACTIVE	\N	2026-06-19 16:54:34.464286	\N	99	\N	\N	t
15	t	17	PUB 2025	2026-06-19 15:06:43.895535	izhar.harahap@orchestria.local	Izhar Harahap	\N	ACTIVE	\N	2026-06-19 16:54:34.552846	\N	99	\N	\N	t
16	t	\N	PUB 2025	2026-06-19 15:06:44.007107	m.faiq.emil.fuadi@orchestria.local	M Faiq Emil Fuadi	\N	ACTIVE	\N	2026-06-19 16:54:34.643406	\N	99	\N	\N	t
17	t	\N	PUB 2025	2026-06-19 15:06:44.159239	alif.rifki.pratama@orchestria.local	Alif Rifki Pratama	\N	ACTIVE	\N	2026-06-19 16:54:34.69142	\N	99	\N	\N	t
18	t	\N	PUB 2025	2026-06-19 15:06:44.315819	dhea.firmasari@orchestria.local	Dhea Firmasari	\N	ACTIVE	\N	2026-06-19 16:54:34.740956	\N	99	\N	\N	t
19	t	\N	PUB 2025	2026-06-19 15:06:44.507964	nabila.monica@orchestria.local	Nabila Monica	\N	ACTIVE	\N	2026-06-19 16:54:34.839513	\N	99	\N	\N	t
20	t	\N	PUB 2025	2026-06-19 15:06:44.590533	ines.karlina@orchestria.local	Ines Karlina	\N	ACTIVE	\N	2026-06-19 16:54:35.124172	\N	99	\N	\N	t
21	t	\N	PUB 2025	2026-06-19 15:06:44.961817	miftahul.jannah.harahap@orchestria.local	Miftahul Jannah Harahap	\N	ACTIVE	\N	2026-06-19 16:54:35.217731	\N	99	\N	\N	t
22	t	\N	PUB 2025	2026-06-19 15:06:45.066382	dea.afrilia@orchestria.local	Dea Afrilia	\N	ACTIVE	\N	2026-06-19 16:54:35.280737	\N	99	\N	\N	t
23	t	\N	PUB 2025	2026-06-19 15:06:45.190939	zaky.setiawan@orchestria.local	Zaky Setiawan	\N	ACTIVE	\N	2026-06-19 16:54:35.411826	\N	99	\N	\N	t
24	t	\N	PUB 2025	2026-06-19 15:06:45.823355	fajar.sidik@orchestria.local	Fajar Sidik	\N	ACTIVE	\N	2026-06-19 16:54:35.582412	\N	99	\N	\N	t
25	t	\N	PUB 2025	2026-06-19 15:06:46.065074	alfarizi@orchestria.local	Alfarizi	\N	ACTIVE	\N	2026-06-19 16:54:35.718486	\N	99	\N	\N	t
26	t	\N	PUB 2025	2026-06-19 15:06:46.241634	apriliyanti@orchestria.local	Apriliyanti	\N	ACTIVE	\N	2026-06-19 16:54:35.80204	\N	99	\N	\N	t
27	t	\N	PUB 2025	2026-06-19 15:06:46.500339	sri.muthian@orchestria.local	Sri Muthian	\N	ACTIVE	\N	2026-06-19 16:54:35.985605	\N	99	\N	\N	t
28	t	\N	PUB 2025	2026-06-19 15:06:46.561893	muhammad.farid@orchestria.local	Muhammad Farid	\N	ACTIVE	\N	2026-06-19 16:54:36.129687	\N	99	\N	\N	t
29	t	\N	PUB 2025	2026-06-19 15:06:46.613894	azhar.farizi@orchestria.local	Azhar Farizi	\N	ACTIVE	\N	2026-06-19 16:54:36.310794	\N	99	\N	\N	t
30	t	\N	PUB 2025	2026-06-19 15:06:46.678487	ewi.lestari.harahap@orchestria.local	Ewi Lestari Harahap	\N	ACTIVE	\N	2026-06-19 16:54:36.372793	\N	99	\N	\N	t
31	t	\N	PUB 2025	2026-06-19 15:06:46.813069	yusri.hasanah@orchestria.local	Yusri Hasanah	\N	ACTIVE	\N	2026-06-19 16:54:36.410342	\N	99	\N	\N	t
32	t	\N	PUB 2025	2026-06-19 15:06:46.882664	ahmad.zaki.hosammido@orchestria.local	Ahmad Zaki Hosammido	\N	ACTIVE	\N	2026-06-19 16:54:36.457343	\N	99	\N	\N	t
33	t	\N	PUB 2025	2026-06-19 15:06:46.954674	ade.dermawan@orchestria.local	Ade Dermawan	\N	ACTIVE	\N	2026-06-19 16:54:36.62544	\N	99	\N	\N	t
34	t	\N	PUB 2025	2026-06-19 15:06:47.182366	m.saroni@orchestria.local	M Saroni	\N	ACTIVE	\N	2026-06-19 16:54:36.826538	\N	99	\N	\N	t
35	t	\N	PUB 2025	2026-06-19 15:06:47.269487	ali.sahroji@orchestria.local	Ali Sahroji	\N	ACTIVE	\N	2026-06-19 16:54:36.860548	\N	99	\N	\N	t
36	t	\N	PUB 2025	2026-06-19 15:06:47.395059	sri.muthia.ningrum@orchestria.local	Sri Muthia Ningrum	\N	ACTIVE	\N	2026-06-19 16:54:36.888566	\N	99	\N	\N	t
37	t	\N	PUB 2025	2026-06-19 15:06:47.506623	galang.ponco.maulana@orchestria.local	Galang Ponco Maulana	\N	ACTIVE	\N	2026-06-19 16:54:36.917104	\N	99	\N	\N	t
38	t	\N	PUB 2025	2026-06-19 15:06:47.573202	padellan.riski@orchestria.local	Padellan Riski	\N	ACTIVE	\N	2026-06-19 16:54:36.991129	\N	99	\N	\N	t
39	t	\N	PUB 2025	2026-06-19 15:06:47.811344	m.haikal@orchestria.local	M Haikal	\N	ACTIVE	\N	2026-06-19 16:54:37.125211	\N	99	\N	\N	t
40	t	\N	PUB 2025	2026-06-19 15:06:47.865922	alfa.rizi@orchestria.local	Alfa Rizi	\N	ACTIVE	\N	2026-06-19 16:54:37.192224	\N	99	\N	\N	t
41	t	\N	PUB 2025	2026-06-19 15:06:47.97552	raja.tegar.albaihaqi@orchestria.local	Raja Tegar Albaihaqi	\N	ACTIVE	\N	2026-06-19 16:54:37.261755	\N	99	\N	\N	t
42	t	\N	PUB 2025	2026-06-19 15:06:48.162658	ulil.arsyad.ramadhan@orchestria.local	Ulil Arsyad Ramadhan	\N	ACTIVE	\N	2026-06-19 16:54:37.323309	\N	99	\N	\N	t
\.


--
-- TOC entry 5169 (class 0 OID 57541)
-- Dependencies: 230
-- Data for Name: organization_periods; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.organization_periods (id, created_at, end_date, active, name, start_date, updated_at, current_period, public_visible) FROM stdin;
1	2026-06-08 01:31:41.261908	2026-08-31	t	PUB 2025/2026	2025-09-01	2026-06-08 01:31:41.261908	t	t
2	2026-06-08 15:06:03.267435	2027-08-31	t	PUB 2026/2027	2026-09-01	2026-06-08 15:06:03.267435	f	t
\.


--
-- TOC entry 5171 (class 0 OID 57552)
-- Dependencies: 232
-- Data for Name: positions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.positions (id, active, code, created_at, description, level_order, name, updated_at, public_visible) FROM stdin;
1	t	KEAMANAN	2026-06-07 06:45:49.699131	Bertanggung jawab atas penertiban, kedisiplinan, dan pengawasan izin keluar mahasiswa PUB.	3	Keamanan	2026-06-07 06:45:49.699131	t
2	t	COACH_INSTRUKTUR	2026-06-07 06:46:00.525136	Membina dan mengarahkan instruktur agar mengajar sesuai kurikulum dan standar pelatihan.	4	Coach Instruktur	2026-06-07 06:46:00.525136	t
3	t	PEMBINA	2026-06-08 01:31:41.541803	Pembina utama organisasi dan pengambil persetujuan akhir.	1	Pembina	2026-06-08 01:31:41.541803	t
4	t	KETUA_PUB	2026-06-08 01:31:41.557357	Pemimpin utama mahasiswa PUB dan penanggung jawab koordinasi organisasi.	2	Ketua PUB	2026-06-08 01:31:41.557357	t
5	t	SEKRETARIS	2026-06-08 01:31:41.573356	Mengelola administrasi, arsip, surat, dan dokumentasi organisasi.	4	Sekretaris	2026-06-08 01:31:41.573356	t
6	t	BENDAHARA_INTERNAL	2026-06-08 01:31:41.588359	Mengelola pengajuan, pencairan internal, bukti pembayaran, dan settlement.	5	Bendahara Internal	2026-06-08 01:31:41.588359	t
7	t	BENDAHARA_EKSTERNAL	2026-06-08 01:31:41.598358	Mencatat laporan keuangan keseluruhan dan penarikan dana dari pihak pembina/yayasan.	6	Bendahara Eksternal	2026-06-08 01:31:41.598358	t
8	t	KETUA_DIVISI	2026-06-08 01:31:41.610358	Memimpin dan bertanggung jawab atas operasional divisi.	7	Ketua Divisi	2026-06-08 01:31:41.610358	t
9	t	KOORDINATOR_DIVISI	2026-06-08 01:31:41.622355	Membantu koordinasi teknis pekerjaan divisi.	8	Koordinator Divisi	2026-06-08 01:31:41.622355	t
10	t	KETUA_ASRAMA	2026-06-08 01:31:41.63336	Mengelola koordinasi keasramaan dan administrasi izin asrama.	9	Ketua Asrama	2026-06-08 01:31:41.63336	t
11	t	INSTRUKTUR	2026-06-08 01:31:41.650901	Mengajar dan membimbing peserta pelatihan sesuai materi yang ditetapkan.	11	Instruktur	2026-06-08 01:31:41.650901	t
12	t	ANGGOTA	2026-06-08 01:31:41.6589	Mahasiswa PUB aktif yang mengikuti kegiatan dan kewajiban organisasi.	99	Anggota	2026-06-08 01:31:41.6589	t
\.


--
-- TOC entry 5182 (class 0 OID 66288)
-- Dependencies: 243
-- Data for Name: public_content_entries; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.public_content_entries (id, active, author_name, author_role, body, category, content_type, created_at, created_by_email, display_order, event_date, link_url, media_url, publication_status, published_at, status_label, subtitle, title, updated_at, updated_by_email) FROM stdin;
\.


--
-- TOC entry 5189 (class 0 OID 0)
-- Dependencies: 233
-- Name: archive_documents_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.archive_documents_id_seq', 2, true);


--
-- TOC entry 5190 (class 0 OID 0)
-- Dependencies: 219
-- Name: division_task_evidences_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.division_task_evidences_id_seq', 1, true);


--
-- TOC entry 5191 (class 0 OID 0)
-- Dependencies: 221
-- Name: division_tasks_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.division_tasks_id_seq', 1, true);


--
-- TOC entry 5192 (class 0 OID 0)
-- Dependencies: 223
-- Name: divisions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.divisions_id_seq', 11, true);


--
-- TOC entry 5193 (class 0 OID 0)
-- Dependencies: 225
-- Name: member_assignments_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.member_assignments_id_seq', 60, true);


--
-- TOC entry 5194 (class 0 OID 0)
-- Dependencies: 227
-- Name: members_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.members_id_seq', 42, true);


--
-- TOC entry 5195 (class 0 OID 0)
-- Dependencies: 229
-- Name: organization_periods_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.organization_periods_id_seq', 2, true);


--
-- TOC entry 5196 (class 0 OID 0)
-- Dependencies: 231
-- Name: positions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.positions_id_seq', 12, true);


--
-- TOC entry 4978 (class 2606 OID 66107)
-- Name: archive_documents archive_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.archive_documents
    ADD CONSTRAINT archive_documents_pkey PRIMARY KEY (id);


--
-- TOC entry 4980 (class 2606 OID 66170)
-- Name: asset_borrowings asset_borrowings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_borrowings
    ADD CONSTRAINT asset_borrowings_pkey PRIMARY KEY (id);


--
-- TOC entry 4982 (class 2606 OID 66187)
-- Name: asset_condition_histories asset_condition_histories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_condition_histories
    ADD CONSTRAINT asset_condition_histories_pkey PRIMARY KEY (id);


--
-- TOC entry 4984 (class 2606 OID 66203)
-- Name: assets assets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT assets_pkey PRIMARY KEY (id);


--
-- TOC entry 4988 (class 2606 OID 66218)
-- Name: cleanliness_assignments cleanliness_assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cleanliness_assignments
    ADD CONSTRAINT cleanliness_assignments_pkey PRIMARY KEY (id);


--
-- TOC entry 4990 (class 2606 OID 66235)
-- Name: cleanliness_point_records cleanliness_point_records_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cleanliness_point_records
    ADD CONSTRAINT cleanliness_point_records_pkey PRIMARY KEY (id);


--
-- TOC entry 4992 (class 2606 OID 66252)
-- Name: cleanliness_schedules cleanliness_schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cleanliness_schedules
    ADD CONSTRAINT cleanliness_schedules_pkey PRIMARY KEY (id);


--
-- TOC entry 4950 (class 2606 OID 57475)
-- Name: division_task_evidences division_task_evidences_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.division_task_evidences
    ADD CONSTRAINT division_task_evidences_pkey PRIMARY KEY (id);


--
-- TOC entry 4952 (class 2606 OID 57493)
-- Name: division_tasks division_tasks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.division_tasks
    ADD CONSTRAINT division_tasks_pkey PRIMARY KEY (id);


--
-- TOC entry 4954 (class 2606 OID 57507)
-- Name: divisions divisions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.divisions
    ADD CONSTRAINT divisions_pkey PRIMARY KEY (id);


--
-- TOC entry 4994 (class 2606 OID 66269)
-- Name: english_activities english_activities_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.english_activities
    ADD CONSTRAINT english_activities_pkey PRIMARY KEY (id);


--
-- TOC entry 4996 (class 2606 OID 66287)
-- Name: english_deposits english_deposits_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.english_deposits
    ADD CONSTRAINT english_deposits_pkey PRIMARY KEY (id);


--
-- TOC entry 4960 (class 2606 OID 57523)
-- Name: member_assignments member_assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.member_assignments
    ADD CONSTRAINT member_assignments_pkey PRIMARY KEY (id);


--
-- TOC entry 4964 (class 2606 OID 57539)
-- Name: members members_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.members
    ADD CONSTRAINT members_pkey PRIMARY KEY (id);


--
-- TOC entry 4968 (class 2606 OID 57550)
-- Name: organization_periods organization_periods_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.organization_periods
    ADD CONSTRAINT organization_periods_pkey PRIMARY KEY (id);


--
-- TOC entry 4972 (class 2606 OID 57565)
-- Name: positions positions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.positions
    ADD CONSTRAINT positions_pkey PRIMARY KEY (id);


--
-- TOC entry 4998 (class 2606 OID 66303)
-- Name: public_content_entries public_content_entries_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.public_content_entries
    ADD CONSTRAINT public_content_entries_pkey PRIMARY KEY (id);


--
-- TOC entry 4974 (class 2606 OID 57579)
-- Name: positions uk3vhyopdpf9huqh1t67ho6nayj; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.positions
    ADD CONSTRAINT uk3vhyopdpf9huqh1t67ho6nayj UNIQUE (name);


--
-- TOC entry 4970 (class 2606 OID 57575)
-- Name: organization_periods uk8ajhkcyi6uqql4xeqkfqvagw8; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.organization_periods
    ADD CONSTRAINT uk8ajhkcyi6uqql4xeqkfqvagw8 UNIQUE (name);


--
-- TOC entry 4966 (class 2606 OID 57573)
-- Name: members uk9d30a9u1qpg8eou0otgkwrp5d; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.members
    ADD CONSTRAINT uk9d30a9u1qpg8eou0otgkwrp5d UNIQUE (email);


--
-- TOC entry 4962 (class 2606 OID 57571)
-- Name: member_assignments uk_member_period_division_position; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.member_assignments
    ADD CONSTRAINT uk_member_period_division_position UNIQUE (member_id, period_id, division_id, position_id);


--
-- TOC entry 4976 (class 2606 OID 57577)
-- Name: positions ukdjkia0ifarv9epmv78bh62r3o; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.positions
    ADD CONSTRAINT ukdjkia0ifarv9epmv78bh62r3o UNIQUE (code);


--
-- TOC entry 4986 (class 2606 OID 66305)
-- Name: assets ukh3rqbypxh7aycu4jdf3sisunv; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT ukh3rqbypxh7aycu4jdf3sisunv UNIQUE (asset_code);


--
-- TOC entry 4956 (class 2606 OID 57567)
-- Name: divisions ukhccml8hpwk506p2l66y8tg1ki; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.divisions
    ADD CONSTRAINT ukhccml8hpwk506p2l66y8tg1ki UNIQUE (code);


--
-- TOC entry 4958 (class 2606 OID 57569)
-- Name: divisions ukoujei63okkb767mmtbv0rrx7p; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.divisions
    ADD CONSTRAINT ukoujei63okkb767mmtbv0rrx7p UNIQUE (name);


--
-- TOC entry 5009 (class 2606 OID 66321)
-- Name: cleanliness_assignments fk19agdiahuvff5ec52qh6vj7jm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cleanliness_assignments
    ADD CONSTRAINT fk19agdiahuvff5ec52qh6vj7jm FOREIGN KEY (schedule_id) REFERENCES public.cleanliness_schedules(id);


--
-- TOC entry 5010 (class 2606 OID 66326)
-- Name: english_deposits fkbji0o62cokwcs7bhs0nm6bi6y; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.english_deposits
    ADD CONSTRAINT fkbji0o62cokwcs7bhs0nm6bi6y FOREIGN KEY (activity_id) REFERENCES public.english_activities(id);


--
-- TOC entry 5002 (class 2606 OID 57600)
-- Name: member_assignments fkcxj9bp7l52xbddgns8ckekeqh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.member_assignments
    ADD CONSTRAINT fkcxj9bp7l52xbddgns8ckekeqh FOREIGN KEY (member_id) REFERENCES public.members(id);


--
-- TOC entry 4999 (class 2606 OID 57580)
-- Name: division_task_evidences fkdchdukb3y9tyxp4n7cstnfnl1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.division_task_evidences
    ADD CONSTRAINT fkdchdukb3y9tyxp4n7cstnfnl1 FOREIGN KEY (task_id) REFERENCES public.division_tasks(id);


--
-- TOC entry 5007 (class 2606 OID 66316)
-- Name: asset_condition_histories fkfqfcm3865mx0kwmx1x1t4jtrm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_condition_histories
    ADD CONSTRAINT fkfqfcm3865mx0kwmx1x1t4jtrm FOREIGN KEY (borrowing_id) REFERENCES public.asset_borrowings(id);


--
-- TOC entry 5000 (class 2606 OID 57585)
-- Name: division_tasks fkis7lfcy7quukggj847lwdb4bw; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.division_tasks
    ADD CONSTRAINT fkis7lfcy7quukggj847lwdb4bw FOREIGN KEY (assigned_member_id) REFERENCES public.members(id);


--
-- TOC entry 5003 (class 2606 OID 57610)
-- Name: member_assignments fkj7k3clwnoxs019kfowyhp0ww1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.member_assignments
    ADD CONSTRAINT fkj7k3clwnoxs019kfowyhp0ww1 FOREIGN KEY (position_id) REFERENCES public.positions(id);


--
-- TOC entry 5001 (class 2606 OID 57590)
-- Name: division_tasks fkp7dvq92hfmlhci8vpcko4605; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.division_tasks
    ADD CONSTRAINT fkp7dvq92hfmlhci8vpcko4605 FOREIGN KEY (division_id) REFERENCES public.divisions(id);


--
-- TOC entry 5006 (class 2606 OID 66306)
-- Name: asset_borrowings fkroq1i3ysa7acfk3j47t5vid1r; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_borrowings
    ADD CONSTRAINT fkroq1i3ysa7acfk3j47t5vid1r FOREIGN KEY (asset_id) REFERENCES public.assets(id);


--
-- TOC entry 5008 (class 2606 OID 66311)
-- Name: asset_condition_histories fkspvelyus68famxr3dk4ech612; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_condition_histories
    ADD CONSTRAINT fkspvelyus68famxr3dk4ech612 FOREIGN KEY (asset_id) REFERENCES public.assets(id);


--
-- TOC entry 5004 (class 2606 OID 57595)
-- Name: member_assignments fktbk49vw0ge14915xj08dvp7ix; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.member_assignments
    ADD CONSTRAINT fktbk49vw0ge14915xj08dvp7ix FOREIGN KEY (division_id) REFERENCES public.divisions(id);


--
-- TOC entry 5005 (class 2606 OID 57605)
-- Name: member_assignments fkto4onj6gmjwcw2dy4c5tcsmej; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.member_assignments
    ADD CONSTRAINT fkto4onj6gmjwcw2dy4c5tcsmej FOREIGN KEY (period_id) REFERENCES public.organization_periods(id);


-- Completed on 2026-06-22 20:57:25

--
-- PostgreSQL database dump complete
--

\unrestrict Kwe9HLPpu5Kxnc3n4SAVuN87TpNiLGFixNfVsynm9Ag6j1oceA4ksd5b5GEURPB

--
-- Database "orchestria_request_db" dump
--

--
-- PostgreSQL database dump
--

\restrict hRHvaJ5t0cvct8reS9Z18KGtYFcHpCGCWrNeQsNUNrYEBgZbPo00mVeICPqzFLb

-- Dumped from database version 18.2
-- Dumped by pg_dump version 18.2

-- Started on 2026-06-22 20:57:25

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5063 (class 1262 OID 57622)
-- Name: orchestria_request_db; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE orchestria_request_db WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'English_Indonesia.1252';


ALTER DATABASE orchestria_request_db OWNER TO postgres;

\unrestrict hRHvaJ5t0cvct8reS9Z18KGtYFcHpCGCWrNeQsNUNrYEBgZbPo00mVeICPqzFLb
\connect orchestria_request_db
\restrict hRHvaJ5t0cvct8reS9Z18KGtYFcHpCGCWrNeQsNUNrYEBgZbPo00mVeICPqzFLb

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 220 (class 1259 OID 57624)
-- Name: fund_requests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fund_requests (
    id bigint NOT NULL,
    active boolean NOT NULL,
    activity_date date,
    completed_at timestamp(6) without time zone,
    created_at timestamp(6) without time zone NOT NULL,
    created_by_email character varying(150) NOT NULL,
    description text,
    division_id bigint NOT NULL,
    division_name character varying(150) NOT NULL,
    priority character varying(50) NOT NULL,
    requester_auth_user_id bigint,
    requester_member_id bigint NOT NULL,
    requester_name character varying(150) NOT NULL,
    status character varying(50) NOT NULL,
    submitted_at timestamp(6) without time zone,
    title character varying(150) NOT NULL,
    total_amount numeric(18,2) NOT NULL,
    updated_at timestamp(6) without time zone,
    updated_by_email character varying(150),
    CONSTRAINT fund_requests_priority_check CHECK (((priority)::text = ANY ((ARRAY['LOW'::character varying, 'MEDIUM'::character varying, 'HIGH'::character varying, 'URGENT'::character varying])::text[]))),
    CONSTRAINT fund_requests_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SUBMITTED'::character varying, 'DIVISION_APPROVED'::character varying, 'PUB_APPROVED'::character varying, 'PEMBINA_APPROVED'::character varying, 'REVISION_REQUESTED'::character varying, 'REJECTED'::character varying, 'READY_FOR_DISBURSEMENT'::character varying, 'DISBURSED'::character varying, 'FUND_RECEIVED'::character varying, 'SETTLEMENT_SUBMITTED'::character varying, 'SETTLEMENT_REVISION_REQUIRED'::character varying, 'SETTLEMENT_APPROVED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.fund_requests OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 57623)
-- Name: fund_requests_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.fund_requests ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.fund_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 224 (class 1259 OID 57667)
-- Name: request_approvals; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.request_approvals (
    id bigint NOT NULL,
    approver_email character varying(150) NOT NULL,
    approver_name character varying(150),
    decided_at timestamp(6) without time zone NOT NULL,
    decision character varying(50) NOT NULL,
    level character varying(50) NOT NULL,
    note text,
    fund_request_id bigint NOT NULL,
    CONSTRAINT request_approvals_decision_check CHECK (((decision)::text = ANY ((ARRAY['APPROVED'::character varying, 'REJECTED'::character varying, 'REVISION_REQUESTED'::character varying])::text[]))),
    CONSTRAINT request_approvals_level_check CHECK (((level)::text = ANY ((ARRAY['DIVISION'::character varying, 'PUB'::character varying, 'PEMBINA'::character varying])::text[])))
);


ALTER TABLE public.request_approvals OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 57666)
-- Name: request_approvals_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.request_approvals ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.request_approvals_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 222 (class 1259 OID 57646)
-- Name: request_items; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.request_items (
    id bigint NOT NULL,
    active boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    description text,
    item_name character varying(150) NOT NULL,
    quantity integer NOT NULL,
    subtotal numeric(18,2) NOT NULL,
    unit_price numeric(18,2) NOT NULL,
    updated_at timestamp(6) without time zone,
    fund_request_id bigint NOT NULL
);


ALTER TABLE public.request_items OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 57645)
-- Name: request_items_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.request_items ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.request_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 228 (class 1259 OID 65674)
-- Name: request_settlements; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.request_settlements (
    id bigint NOT NULL,
    active boolean NOT NULL,
    approved_at timestamp(6) without time zone,
    approved_by_email character varying(150),
    created_at timestamp(6) without time zone NOT NULL,
    note text,
    proof_url character varying(500),
    remaining_amount numeric(18,2) NOT NULL,
    shortage_amount numeric(18,2) NOT NULL,
    spent_amount numeric(18,2) NOT NULL,
    submitted_at timestamp(6) without time zone NOT NULL,
    submitted_by_email character varying(150) NOT NULL,
    updated_at timestamp(6) without time zone,
    fund_request_id bigint NOT NULL,
    status character varying(30),
    submission_count integer DEFAULT 1,
    revision_count integer DEFAULT 0,
    last_revision_note text,
    reviewed_by_email character varying(150),
    reviewed_at timestamp without time zone,
    lock_version bigint DEFAULT 0
);


ALTER TABLE public.request_settlements OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 65673)
-- Name: request_settlements_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.request_settlements ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.request_settlements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 226 (class 1259 OID 57688)
-- Name: request_status_histories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.request_status_histories (
    id bigint NOT NULL,
    changed_at timestamp(6) without time zone NOT NULL,
    changed_by_email character varying(150) NOT NULL,
    new_status character varying(50) NOT NULL,
    note text,
    old_status character varying(50),
    fund_request_id bigint NOT NULL,
    CONSTRAINT request_status_histories_new_status_check CHECK (((new_status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SUBMITTED'::character varying, 'DIVISION_APPROVED'::character varying, 'PUB_APPROVED'::character varying, 'PEMBINA_APPROVED'::character varying, 'REVISION_REQUESTED'::character varying, 'REJECTED'::character varying, 'READY_FOR_DISBURSEMENT'::character varying, 'DISBURSED'::character varying, 'FUND_RECEIVED'::character varying, 'SETTLEMENT_SUBMITTED'::character varying, 'SETTLEMENT_REVISION_REQUIRED'::character varying, 'SETTLEMENT_APPROVED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT request_status_histories_old_status_check CHECK (((old_status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SUBMITTED'::character varying, 'DIVISION_APPROVED'::character varying, 'PUB_APPROVED'::character varying, 'PEMBINA_APPROVED'::character varying, 'REVISION_REQUESTED'::character varying, 'REJECTED'::character varying, 'READY_FOR_DISBURSEMENT'::character varying, 'DISBURSED'::character varying, 'FUND_RECEIVED'::character varying, 'SETTLEMENT_SUBMITTED'::character varying, 'SETTLEMENT_REVISION_REQUIRED'::character varying, 'SETTLEMENT_APPROVED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.request_status_histories OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 57687)
-- Name: request_status_histories_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.request_status_histories ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.request_status_histories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 5049 (class 0 OID 57624)
-- Dependencies: 220
-- Data for Name: fund_requests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fund_requests (id, active, activity_date, completed_at, created_at, created_by_email, description, division_id, division_name, priority, requester_auth_user_id, requester_member_id, requester_name, status, submitted_at, title, total_amount, updated_at, updated_by_email) FROM stdin;
1	t	2026-06-10	2026-06-16 02:54:38.085184	2026-06-16 00:20:42.970315	superadmin@orchestria.local	Konsumsi untuk rapat koordinasi divisi	1	Divisi Kesejahteraan	MEDIUM	1	1	Pangeran Valerensco Rivaldi Hutabarat	COMPLETED	2026-06-16 00:26:19.340205	Pengajuan Konsumsi Rapat Divisi	250000.00	2026-06-16 02:54:38.088185	superadmin@orchestria.local
4	t	2026-06-20	2026-06-20 00:34:07.554095	2026-06-19 15:43:03.725671	izhar.harahap@orchestria.local	tes flow	1	Divisi Pendidikan dan Pelatihan	MEDIUM	17	15	Izhar Harahap	COMPLETED	2026-06-19 15:46:46.412008	tes flow	8000.00	2026-06-20 00:34:07.562671	andini.siti.nuriyanti@orchestria.local
5	t	2026-06-20	\N	2026-06-20 17:33:13.077279	rickhy.ramadhan@orchestria.local	im broke nigga	1	Divisi Pendidikan dan Pelatihan	HIGH	13	11	Rickhy Ramadhan	DRAFT	\N	Gaji Koordinator	0.00	2026-06-20 17:33:13.077279	rickhy.ramadhan@orchestria.local
3	t	2026-10-10	\N	2026-06-19 13:37:06.375399	superadmin@orchestria.local	sese	1	Divisi Pendidikan dan Pelatihan	MEDIUM	2	2	Super Admin Orchestria	REVISION_REQUESTED	2026-06-19 13:37:32.502061	tesss	2000.00	2026-06-19 13:37:59.679795	superadmin@orchestria.local
2	t	\N	\N	2026-06-19 01:42:07.873727	superadmin@orchestria.local	\N	1	Divisi Pendidikan dan Pelatihan	MEDIUM	2	2	Super Admin Orchestria	READY_FOR_DISBURSEMENT	2026-06-19 02:24:28.777637	Konsumsi Pelatihan Java	55000.00	2026-06-19 13:38:09.429082	superadmin@orchestria.local
\.


--
-- TOC entry 5053 (class 0 OID 57667)
-- Dependencies: 224
-- Data for Name: request_approvals; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.request_approvals (id, approver_email, approver_name, decided_at, decision, level, note, fund_request_id) FROM stdin;
1	superadmin@orchestria.local	Ketua Divisi Kesejahteraan	2026-06-16 00:40:12.537283	APPROVED	DIVISION	Disetujui oleh Divisi	1
2	superadmin@orchestria.local	Ketua Divisi Kesejahteraan	2026-06-16 00:41:49.840003	APPROVED	PUB	Disetujui Ketua PUB	1
3	superadmin@orchestria.local	Abdul Hafiz Tanjung	2026-06-16 00:42:19.703512	APPROVED	PEMBINA	Disetujui Pembina	1
4	superadmin@orchestria.local	Super Admin Orchestria	2026-06-19 09:54:44.444873	APPROVED	DIVISION	oke	2
5	superadmin@orchestria.local	Super Admin Orchestria	2026-06-19 13:37:45.275022	APPROVED	DIVISION	aman	3
6	superadmin@orchestria.local	Super Admin Orchestria	2026-06-19 13:37:50.735743	APPROVED	PUB	asas	2
7	superadmin@orchestria.local	Super Admin Orchestria	2026-06-19 13:37:59.606368	REVISION_REQUESTED	PUB	ini ga mantap	3
8	superadmin@orchestria.local	Super Admin Orchestria	2026-06-19 13:38:09.256902	APPROVED	PEMBINA	ok	2
9	rickhy.ramadhan@orchestria.local	Rickhy Ramadhan	2026-06-19 15:47:26.394335	APPROVED	DIVISION	mntp	4
10	pangeran.valerensco.rivaldi.hutabarat@orchestria.local	Pangeran Valerensco Rivaldi Hutabarat	2026-06-19 15:49:05.600615	APPROVED	PUB	oke	4
11	abdul.hafiz.tanjung@orchestria.local	Abdul Hafiz Tanjung	2026-06-19 15:50:06.733743	APPROVED	PEMBINA	as	4
\.


--
-- TOC entry 5051 (class 0 OID 57646)
-- Dependencies: 222
-- Data for Name: request_items; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.request_items (id, active, created_at, description, item_name, quantity, subtotal, unit_price, updated_at, fund_request_id) FROM stdin;
1	t	2026-06-16 00:21:11.376033	Konsumsi peserta rapat	Nasi Box	10	250000.00	25000.00	2026-06-16 00:21:11.376033	1
2	t	2026-06-19 02:22:53.56584	Konsumsi Pelatihan	Konsumsi Peserta	10	25000.00	2500.00	2026-06-19 02:22:53.56584	2
3	t	2026-06-19 02:24:16.223484	121	tres	1	30000.00	30000.00	2026-06-19 02:24:16.223484	2
4	t	2026-06-19 13:37:28.483489	asa	sasa	1	2000.00	2000.00	2026-06-19 13:37:28.483489	3
5	t	2026-06-19 15:46:41.88218	asa	kue	2	8000.00	4000.00	2026-06-19 15:46:41.88218	4
\.


--
-- TOC entry 5057 (class 0 OID 65674)
-- Dependencies: 228
-- Data for Name: request_settlements; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.request_settlements (id, active, approved_at, approved_by_email, created_at, note, proof_url, remaining_amount, shortage_amount, spent_amount, submitted_at, submitted_by_email, updated_at, fund_request_id, status, submission_count, revision_count, last_revision_note, reviewed_by_email, reviewed_at, lock_version) FROM stdin;
1	t	2026-06-16 02:54:38.084185	superadmin@orchestria.local	2026-06-16 02:53:52.049666	Dana digunakan untuk konsumsi rapat, sisa 10000	https://example.com/struk.jpg	10000.00	0.00	240000.00	2026-06-16 02:53:52.049666	superadmin@orchestria.local	2026-06-16 02:54:38.088185	1	\N	1	0	\N	\N	\N	0
2	t	2026-06-20 00:34:07.554095	andini.siti.nuriyanti@orchestria.local	2026-06-19 16:22:53.029678	Laporan diperbaiki dan bukti penggunaan dilampirkan.	https://drive.google.com/contoh-bukti	0.01	0.00	7999.99	2026-06-20 00:33:50.630788	izhar.harahap@orchestria.local	2026-06-20 00:34:07.56367	4	APPROVED	3	2	manaaa?	andini.siti.nuriyanti@orchestria.local	2026-06-20 00:34:07.554095	4
\.


--
-- TOC entry 5055 (class 0 OID 57688)
-- Dependencies: 226
-- Data for Name: request_status_histories; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.request_status_histories (id, changed_at, changed_by_email, new_status, note, old_status, fund_request_id) FROM stdin;
1	2026-06-16 00:26:19.341207	superadmin@orchestria.local	SUBMITTED	Pengajuan dana disubmit	DRAFT	1
2	2026-06-16 00:40:12.587311	superadmin@orchestria.local	DIVISION_APPROVED	Pengajuan disetujui pada level DIVISION	SUBMITTED	1
3	2026-06-16 00:41:49.844007	superadmin@orchestria.local	PUB_APPROVED	Pengajuan disetujui pada level PUB	DIVISION_APPROVED	1
4	2026-06-16 00:42:19.710503	superadmin@orchestria.local	READY_FOR_DISBURSEMENT	Pengajuan disetujui pada level PEMBINA	PUB_APPROVED	1
5	2026-06-16 02:29:42.203349	superadmin@orchestria.local	DISBURSED	Dana pengajuan sudah dicairkan oleh finance-service	READY_FOR_DISBURSEMENT	1
6	2026-06-16 02:53:22.31189	superadmin@orchestria.local	FUND_RECEIVED	Dana pengajuan sudah dikonfirmasi diterima	DISBURSED	1
7	2026-06-16 02:53:52.055665	superadmin@orchestria.local	SETTLEMENT_SUBMITTED	Settlement penggunaan dana dikirim	FUND_RECEIVED	1
8	2026-06-16 02:54:38.085184	superadmin@orchestria.local	COMPLETED	Settlement disetujui dan pengajuan selesai	SETTLEMENT_SUBMITTED	1
9	2026-06-19 02:24:28.779638	superadmin@orchestria.local	SUBMITTED	Pengajuan dana disubmit	DRAFT	2
10	2026-06-19 09:54:44.890699	superadmin@orchestria.local	DIVISION_APPROVED	Pengajuan disetujui pada level DIVISION	SUBMITTED	2
11	2026-06-19 13:37:32.511598	superadmin@orchestria.local	SUBMITTED	Pengajuan dana disubmit	DRAFT	3
12	2026-06-19 13:37:45.291582	superadmin@orchestria.local	DIVISION_APPROVED	Pengajuan disetujui pada level DIVISION	SUBMITTED	3
13	2026-06-19 13:37:50.949108	superadmin@orchestria.local	PUB_APPROVED	Pengajuan disetujui pada level PUB	DIVISION_APPROVED	2
14	2026-06-19 13:37:59.62746	superadmin@orchestria.local	REVISION_REQUESTED	ini ga mantap	DIVISION_APPROVED	3
15	2026-06-19 13:38:09.324752	superadmin@orchestria.local	READY_FOR_DISBURSEMENT	Pengajuan disetujui pada level PEMBINA	PUB_APPROVED	2
16	2026-06-19 15:46:46.417007	izhar.harahap@orchestria.local	SUBMITTED	Pengajuan dana disubmit	DRAFT	4
17	2026-06-19 15:47:26.409338	rickhy.ramadhan@orchestria.local	DIVISION_APPROVED	Pengajuan disetujui pada level DIVISION	SUBMITTED	4
18	2026-06-19 15:49:05.644191	pangeran.valerensco.rivaldi.hutabarat@orchestria.local	PUB_APPROVED	Pengajuan disetujui pada level PUB	DIVISION_APPROVED	4
19	2026-06-19 15:50:06.749269	abdul.hafiz.tanjung@orchestria.local	READY_FOR_DISBURSEMENT	Pengajuan disetujui pada level PEMBINA	PUB_APPROVED	4
20	2026-06-19 15:52:23.059672	andini.siti.nuriyanti@orchestria.local	DISBURSED	Dana pengajuan sudah dicairkan oleh finance-service	READY_FOR_DISBURSEMENT	4
21	2026-06-19 16:22:43.265896	izhar.harahap@orchestria.local	FUND_RECEIVED	Dana pengajuan sudah dikonfirmasi diterima	DISBURSED	4
22	2026-06-19 16:22:53.071674	izhar.harahap@orchestria.local	SETTLEMENT_SUBMITTED	Settlement penggunaan dana dikirim	FUND_RECEIVED	4
25	2026-06-19 23:43:58.275525	izhar.harahap@orchestria.local	SETTLEMENT_SUBMITTED	Settlement penggunaan dana diperbaiki dan dikirim ulang	SETTLEMENT_REVISION_REQUIRED	4
26	2026-06-20 00:33:36.664102	andini.siti.nuriyanti@orchestria.local	SETTLEMENT_REVISION_REQUIRED	Settlement membutuhkan revisi: manaaa?	SETTLEMENT_SUBMITTED	4
27	2026-06-20 00:33:50.631785	izhar.harahap@orchestria.local	SETTLEMENT_SUBMITTED	Settlement penggunaan dana diperbaiki dan dikirim ulang	SETTLEMENT_REVISION_REQUIRED	4
28	2026-06-20 00:34:07.555642	andini.siti.nuriyanti@orchestria.local	COMPLETED	Settlement disetujui dan pengajuan selesai	SETTLEMENT_SUBMITTED	4
\.


--
-- TOC entry 5064 (class 0 OID 0)
-- Dependencies: 219
-- Name: fund_requests_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.fund_requests_id_seq', 5, true);


--
-- TOC entry 5065 (class 0 OID 0)
-- Dependencies: 223
-- Name: request_approvals_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.request_approvals_id_seq', 11, true);


--
-- TOC entry 5066 (class 0 OID 0)
-- Dependencies: 221
-- Name: request_items_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.request_items_id_seq', 5, true);


--
-- TOC entry 5067 (class 0 OID 0)
-- Dependencies: 227
-- Name: request_settlements_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.request_settlements_id_seq', 2, true);


--
-- TOC entry 5068 (class 0 OID 0)
-- Dependencies: 225
-- Name: request_status_histories_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.request_status_histories_id_seq', 28, true);


--
-- TOC entry 4886 (class 2606 OID 57644)
-- Name: fund_requests fund_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fund_requests
    ADD CONSTRAINT fund_requests_pkey PRIMARY KEY (id);


--
-- TOC entry 4890 (class 2606 OID 57681)
-- Name: request_approvals request_approvals_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.request_approvals
    ADD CONSTRAINT request_approvals_pkey PRIMARY KEY (id);


--
-- TOC entry 4888 (class 2606 OID 57660)
-- Name: request_items request_items_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.request_items
    ADD CONSTRAINT request_items_pkey PRIMARY KEY (id);


--
-- TOC entry 4894 (class 2606 OID 65689)
-- Name: request_settlements request_settlements_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.request_settlements
    ADD CONSTRAINT request_settlements_pkey PRIMARY KEY (id);


--
-- TOC entry 4892 (class 2606 OID 57701)
-- Name: request_status_histories request_status_histories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.request_status_histories
    ADD CONSTRAINT request_status_histories_pkey PRIMARY KEY (id);


--
-- TOC entry 4896 (class 2606 OID 65691)
-- Name: request_settlements uk43nd97ey9ku4mpgyena8h74n7; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.request_settlements
    ADD CONSTRAINT uk43nd97ey9ku4mpgyena8h74n7 UNIQUE (fund_request_id);


--
-- TOC entry 4900 (class 2606 OID 65692)
-- Name: request_settlements fk5e2o5wfidw5d3tgq3oc1vnf3t; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.request_settlements
    ADD CONSTRAINT fk5e2o5wfidw5d3tgq3oc1vnf3t FOREIGN KEY (fund_request_id) REFERENCES public.fund_requests(id);


--
-- TOC entry 4899 (class 2606 OID 57702)
-- Name: request_status_histories fkkcekygtcdtp7t5fqaopk7rhg7; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.request_status_histories
    ADD CONSTRAINT fkkcekygtcdtp7t5fqaopk7rhg7 FOREIGN KEY (fund_request_id) REFERENCES public.fund_requests(id);


--
-- TOC entry 4897 (class 2606 OID 57661)
-- Name: request_items fkqw8r1rhqugy1v8l3hxgjlxst9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.request_items
    ADD CONSTRAINT fkqw8r1rhqugy1v8l3hxgjlxst9 FOREIGN KEY (fund_request_id) REFERENCES public.fund_requests(id);


--
-- TOC entry 4898 (class 2606 OID 57682)
-- Name: request_approvals fksof4q841pltwt93p6wac19577; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.request_approvals
    ADD CONSTRAINT fksof4q841pltwt93p6wac19577 FOREIGN KEY (fund_request_id) REFERENCES public.fund_requests(id);


-- Completed on 2026-06-22 20:57:26

--
-- PostgreSQL database dump complete
--

\unrestrict hRHvaJ5t0cvct8reS9Z18KGtYFcHpCGCWrNeQsNUNrYEBgZbPo00mVeICPqzFLb

-- Completed on 2026-06-22 20:57:26

--
-- PostgreSQL database cluster dump complete
--


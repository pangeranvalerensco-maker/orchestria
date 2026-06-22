--
-- PostgreSQL database dump
--

\restrict shEHp4ERK7OEIAEJn0uQAk2AgRxT3U8I1Wi5SgQ6M3s6fBVlFmbmdKqkbipn373

-- Dumped from database version 18.2
-- Dumped by pg_dump version 18.2

-- Started on 2026-06-22 23:50:29

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

\unrestrict shEHp4ERK7OEIAEJn0uQAk2AgRxT3U8I1Wi5SgQ6M3s6fBVlFmbmdKqkbipn373
\connect orchestria_notification_db
\restrict shEHp4ERK7OEIAEJn0uQAk2AgRxT3U8I1Wi5SgQ6M3s6fBVlFmbmdKqkbipn373

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

INSERT INTO public.notification_logs VALUES ('bc34a72a-0c07-4d63-990a-91c8041a49f0', 1, NULL, 'Kode OTP login Anda: 724577

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 20:58:40.075226', 'system', false, '2026-06-22 20:58:40.084225', NULL, NULL, '2026-06-22 20:58:44.520001', 'SENT', 'Kode verifikasi login Orchestria', 'pangeran.valerensco.rivaldi.hutabarat@orchestria.local');
INSERT INTO public.notification_logs VALUES ('f7d8f7b9-d20f-424a-83e4-06cb4ef85d0c', 1, NULL, 'Kode OTP konfirmasi keamanan Anda: 679149

Berlaku 5 menit.', NULL, '2026-06-22 21:07:10.19161', 'system', false, '2026-06-22 21:07:10.197144', NULL, NULL, '2026-06-22 21:07:13.747794', 'SENT', 'Kode konfirmasi keamanan Orchestria', 'firman.suherman@orchestria.local');
INSERT INTO public.notification_logs VALUES ('2379c573-4087-4c01-8490-ae5e66095dc5', 1, NULL, 'Kode OTP konfirmasi keamanan Anda: 414418

Berlaku 5 menit.', NULL, '2026-06-22 21:07:13.587794', 'system', false, '2026-06-22 21:07:13.590796', NULL, NULL, '2026-06-22 21:07:16.80385', 'SENT', 'Kode konfirmasi keamanan Orchestria', 'firman.suherman@orchestria.local');
INSERT INTO public.notification_logs VALUES ('f4047f58-8162-456c-b650-bd133d5d8c7a', 1, NULL, 'Kode OTP konfirmasi keamanan Anda: 872655

Berlaku 5 menit.', NULL, '2026-06-22 21:07:45.86826', 'system', false, '2026-06-22 21:07:45.871265', NULL, NULL, '2026-06-22 21:07:49.096864', 'SENT', 'Kode konfirmasi keamanan Orchestria', 'firman.suherman@orchestria.local');
INSERT INTO public.notification_logs VALUES ('4ffe5915-be0e-4acd-9afe-bb91475629db', 1, NULL, 'Kode OTP konfirmasi keamanan Anda: 481323

Berlaku 5 menit.', NULL, '2026-06-22 21:07:49.320027', 'system', false, '2026-06-22 21:07:49.324019', NULL, NULL, '2026-06-22 21:07:52.643199', 'SENT', 'Kode konfirmasi keamanan Orchestria', 'firman.suherman@orchestria.local');
INSERT INTO public.notification_logs VALUES ('82881f7a-1a2a-44aa-bd38-2343484a4ab1', 1, NULL, 'Kode OTP konfirmasi keamanan Anda: 503024

Berlaku 5 menit.', NULL, '2026-06-22 21:07:49.400996', 'system', false, '2026-06-22 21:07:49.406014', NULL, NULL, '2026-06-22 21:07:52.871948', 'SENT', 'Kode konfirmasi keamanan Orchestria', 'firman.suherman@orchestria.local');
INSERT INTO public.notification_logs VALUES ('02598a66-30fc-475d-a5f6-12f984f1e3b7', 1, NULL, 'Kode OTP login Anda: 770510

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 21:08:59.518663', 'system', false, '2026-06-22 21:08:59.522663', NULL, NULL, '2026-06-22 21:09:02.995955', 'SENT', 'Kode verifikasi login Orchestria', 'pangeran.valerensco.rivaldi.hutabarat@orchestria.local');
INSERT INTO public.notification_logs VALUES ('3077e77b-9fcc-4a47-86e5-d64b46294df9', 1, NULL, 'Kode OTP reset password: 943636

Berlaku 5 menit. Jangan bagikan.', NULL, '2026-06-22 21:19:35.354352', 'system', false, '2026-06-22 21:19:35.358351', NULL, NULL, '2026-06-22 21:19:39.093442', 'SENT', 'Kode reset password Orchestria', 'pangeranvalerensco@gmail.com');
INSERT INTO public.notification_logs VALUES ('be5457b5-2e64-4a8b-a2a3-49c21b6c6d72', 1, NULL, 'Kode OTP login Anda: 467975

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 21:20:14.853837', 'system', false, '2026-06-22 21:20:14.857838', NULL, NULL, '2026-06-22 21:20:18.083976', 'SENT', 'Kode verifikasi login Orchestria', 'pangeran.valerensco.rivaldi.hutabarat@orchestria.local');
INSERT INTO public.notification_logs VALUES ('845fad32-64d7-4912-a8d8-271d00804d23', 1, NULL, 'Kode OTP login Anda: 333725

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 22:09:02.998585', 'system', false, '2026-06-22 22:09:03.00958', NULL, NULL, '2026-06-22 22:09:08.707077', 'SENT', 'Kode verifikasi login Orchestria', 'admin@mailinator.com');
INSERT INTO public.notification_logs VALUES ('5219f6e8-10d9-4ad7-b413-b8ecf1c21b1f', 1, NULL, 'Kode OTP reset password: 181347

Berlaku 5 menit. Jangan bagikan.', NULL, '2026-06-22 22:14:34.918873', 'system', false, '2026-06-22 22:14:34.922869', NULL, NULL, '2026-06-22 22:14:38.79515', 'SENT', 'Kode reset password Orchestria', 'admin@mailinator.com');
INSERT INTO public.notification_logs VALUES ('f2ec469b-8b53-4ca1-be2e-453b13e2dd20', 1, NULL, 'Kode OTP reset password: 968307

Berlaku 5 menit. Jangan bagikan.', NULL, '2026-06-22 22:15:19.627656', 'system', false, '2026-06-22 22:15:19.631654', NULL, NULL, '2026-06-22 22:15:23.24266', 'SENT', 'Kode reset password Orchestria', 'admin@mailinator.com');
INSERT INTO public.notification_logs VALUES ('043af53d-8dcf-482e-bbc9-3b088a909c13', 1, NULL, 'Kode OTP reset password: 238176

Berlaku 5 menit. Jangan bagikan.', NULL, '2026-06-22 22:15:46.138736', 'system', false, '2026-06-22 22:15:46.141744', NULL, NULL, '2026-06-22 22:15:49.378096', 'SENT', 'Kode reset password Orchestria', 'izhar.harahap@mailinator.com');
INSERT INTO public.notification_logs VALUES ('f1a066cc-11a5-4753-94af-54737e4b94e7', 1, NULL, 'Kode OTP konfirmasi keamanan Anda: 225473

Berlaku 5 menit.', NULL, '2026-06-22 22:16:43.643133', 'system', false, '2026-06-22 22:16:43.64815', NULL, NULL, '2026-06-22 22:16:47.452654', 'SENT', 'Kode konfirmasi keamanan Orchestria', 'izhar.harahap@mailinator.com');
INSERT INTO public.notification_logs VALUES ('cd6a6339-658c-4800-91a7-69b73510f732', 1, NULL, 'Kode OTP konfirmasi keamanan Anda: 983543

Berlaku 5 menit.', NULL, '2026-06-22 22:16:46.158256', 'system', false, '2026-06-22 22:16:46.167953', NULL, NULL, '2026-06-22 22:16:49.96226', 'SENT', 'Kode konfirmasi keamanan Orchestria', 'izhar.harahap@mailinator.com');
INSERT INTO public.notification_logs VALUES ('71801820-deee-4579-857b-c7da5b9408af', 1, NULL, 'Kode OTP login Anda: 956423

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 22:25:50.540136', 'system', false, '2026-06-22 22:25:50.563675', NULL, NULL, '2026-06-22 22:25:57.177355', 'SENT', 'Kode verifikasi login Orchestria', 'pangeran.valerensco.rivaldi.hutabarat@mailinator.com');
INSERT INTO public.notification_logs VALUES ('ca36e568-6147-4424-82ed-09b5f084859d', 1, NULL, 'Kode OTP login Anda: 507051

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 22:37:58.513022', 'system', false, '2026-06-22 22:37:58.515949', NULL, NULL, '2026-06-22 22:38:05.414008', 'SENT', 'Kode verifikasi login Orchestria', 'pangeran.valerensco.rivaldi.hutabarat@mailinator.com');
INSERT INTO public.notification_logs VALUES ('601a6393-550f-472d-82a9-744e4a16e288', 1, NULL, 'Kode OTP reset password: 788395

Berlaku 5 menit. Jangan bagikan.', NULL, '2026-06-22 22:50:34.008158', 'system', false, '2026-06-22 22:50:34.015155', NULL, NULL, '2026-06-22 22:50:41.115312', 'SENT', 'Kode reset password Orchestria', 'pangeran.valerensco.rivaldi.hutabarat@mailinator.com');
INSERT INTO public.notification_logs VALUES ('79d4d189-24fd-4922-b027-d399b424446f', 1, NULL, 'Kode OTP login Anda: 919781

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 22:51:21.16714', 'system', false, '2026-06-22 22:51:21.172162', NULL, NULL, '2026-06-22 22:51:25.012833', 'SENT', 'Kode verifikasi login Orchestria', 'pangeran.valerensco.rivaldi.hutabarat@mailinator.com');
INSERT INTO public.notification_logs VALUES ('5ad51e41-f43c-48fd-a32c-877ec54b26dc', 1, NULL, 'Kode OTP login Anda: 929626

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 23:03:49.037106', 'system', false, '2026-06-22 23:03:49.042103', NULL, NULL, '2026-06-22 23:04:01.412542', 'SENT', 'Kode verifikasi login Orchestria', 'izhar.harahap@mailinator.com');
INSERT INTO public.notification_logs VALUES ('75730e10-0ce8-4efd-a0c8-051b8f595b4b', 1, NULL, 'Kode OTP login Anda: 685439

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 23:11:27.638654', 'system', false, '2026-06-22 23:11:27.65167', NULL, NULL, '2026-06-22 23:11:32.136655', 'SENT', 'Kode verifikasi login Orchestria', 'izhar.harahap@mailinator.com');
INSERT INTO public.notification_logs VALUES ('a628745a-7dbb-4d1a-856f-ccaaab1b941a', 1, NULL, 'Kode OTP login Anda: 659275

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 23:15:51.466787', 'system', false, '2026-06-22 23:15:51.472267', NULL, NULL, '2026-06-22 23:15:58.584313', 'SENT', 'Kode verifikasi login Orchestria', 'izhar.harahap@mailinator.com');
INSERT INTO public.notification_logs VALUES ('85b360e5-65e7-4049-b183-4153ad78651b', 1, NULL, 'Kode OTP login Anda: 874902

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 23:17:20.035777', 'system', false, '2026-06-22 23:17:20.039779', NULL, NULL, '2026-06-22 23:17:26.985433', 'SENT', 'Kode verifikasi login Orchestria', 'pangeran.valerensco.rivaldi.hutabarat@mailinator.com');
INSERT INTO public.notification_logs VALUES ('386d8816-a1e6-4f07-9cf1-b09a420d5a37', 1, NULL, 'Kode OTP login Anda: 465333

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 23:18:14.848519', 'system', false, '2026-06-22 23:18:14.852518', NULL, NULL, '2026-06-22 23:18:18.719535', 'SENT', 'Kode verifikasi login Orchestria', 'izhar.harahap@mailinator.com');
INSERT INTO public.notification_logs VALUES ('cbd09dd3-d15e-4866-ad9e-bb213cfc1dec', 1, NULL, 'Kode OTP login Anda: 407777

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 23:19:34.793824', 'system', false, '2026-06-22 23:19:34.801978', NULL, NULL, '2026-06-22 23:19:38.11376', 'SENT', 'Kode verifikasi login Orchestria', 'abdul.hafiz.tanjung@mailinator.com');
INSERT INTO public.notification_logs VALUES ('5f0da34f-b14f-485d-9b02-841ad4d59511', 1, NULL, 'Kode OTP login Anda: 222705

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 23:20:33.24935', 'system', false, '2026-06-22 23:20:33.269764', NULL, NULL, '2026-06-22 23:20:36.688454', 'SENT', 'Kode verifikasi login Orchestria', 'andini.siti.nuriyanti@mailinator.com');
INSERT INTO public.notification_logs VALUES ('99a56c76-02b8-48b0-a01f-38bcaed59105', 1, NULL, 'Kode OTP login Anda: 134095

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 23:22:47.751063', 'system', false, '2026-06-22 23:22:47.798352', NULL, NULL, '2026-06-22 23:22:51.645857', 'SENT', 'Kode verifikasi login Orchestria', 'andini.siti.nuriyanti@mailinator.com');
INSERT INTO public.notification_logs VALUES ('77016f1a-eada-4a7b-9ab3-526a58391823', 1, NULL, 'Kode OTP login Anda: 498359

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 23:38:35.256296', 'system', false, '2026-06-22 23:38:35.261336', NULL, NULL, '2026-06-22 23:38:39.852923', 'SENT', 'Kode verifikasi login Orchestria', 'admin@mailinator.com');
INSERT INTO public.notification_logs VALUES ('a528f14e-51ae-45a5-84e2-cbb18c73f793', 1, NULL, 'Kode OTP login Anda: 893195

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 23:39:03.465141', 'system', false, '2026-06-22 23:39:03.468144', NULL, NULL, '2026-06-22 23:39:06.510423', 'SENT', 'Kode verifikasi login Orchestria', 'admin@mailinator.com');
INSERT INTO public.notification_logs VALUES ('084e7729-2722-4ed8-9d8e-1518499a8f25', 1, NULL, 'Kode OTP Anda: 937702

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 23:41:56.894603', 'system', false, '2026-06-22 23:41:56.899624', NULL, NULL, '2026-06-22 23:41:59.984189', 'SENT', 'Kode verifikasi login Orchestria', 'admin@mailinator.com');
INSERT INTO public.notification_logs VALUES ('1de1a740-4dcc-4014-9fd7-50060e877d92', 1, NULL, 'Kode OTP login Anda: 195993

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 23:42:19.771252', 'system', false, '2026-06-22 23:42:19.775255', NULL, NULL, '2026-06-22 23:42:22.928847', 'SENT', 'Kode verifikasi login Orchestria', 'admin@mailinator.com');
INSERT INTO public.notification_logs VALUES ('d28b944b-fe62-4ec6-a8a9-e25870c812f9', 1, NULL, 'Kode OTP login Anda: 145448

Berlaku 5 menit. Jangan bagikan kode ini.', NULL, '2026-06-22 23:43:00.596665', 'system', false, '2026-06-22 23:43:00.600173', NULL, NULL, '2026-06-22 23:43:03.658464', 'SENT', 'Kode verifikasi login Orchestria', 'pangeran.valerensco.rivaldi.hutabarat@mailinator.com');


--
-- TOC entry 5031 (class 0 OID 66350)
-- Dependencies: 220
-- Data for Name: report_export_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5032 (class 0 OID 66364)
-- Dependencies: 221
-- Data for Name: report_subscribers; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5033 (class 0 OID 66378)
-- Dependencies: 222
-- Data for Name: scheduled_job_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.scheduled_job_logs VALUES ('90d3e401-0218-4148-aceb-390e7d6e899e', NULL, '2026-06-22 20:55:54.324959', 'Health Ping', 'Job executed successfully', '2026-06-22 20:55:54.269317', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('fcba07bb-61b1-4a98-ad54-6fd797e6efd0', NULL, '2026-06-22 20:55:55.030902', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 20:55:54.557446', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('ee1a77c9-7332-4215-a1ed-142af6d9003b', NULL, '2026-06-22 21:00:54.234499', 'Health Ping', 'Job executed successfully', '2026-06-22 21:00:54.231527', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('e8cc1238-67ce-4813-bd1a-c3d3eb5d3f80', NULL, '2026-06-22 21:05:54.354588', 'Health Ping', 'Job executed successfully', '2026-06-22 21:05:54.230117', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('09d4bfa2-b274-4f57-a904-8f65165e392e', NULL, '2026-06-22 21:05:55.055086', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 21:05:55.040074', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('818edb99-fcdf-4a34-bdfc-e59a49e5efa8', NULL, '2026-06-22 21:10:54.241707', 'Health Ping', 'Job executed successfully', '2026-06-22 21:10:54.231702', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('2524cb6c-a802-402b-b8a2-409be3f047a8', NULL, '2026-06-22 21:15:54.238394', 'Health Ping', 'Job executed successfully', '2026-06-22 21:15:54.235398', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('16ca3928-c6e7-42e2-8ff3-38e54548138b', NULL, '2026-06-22 21:15:55.068551', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 21:15:55.065025', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('b26e10be-7552-4b4b-ad54-61abd65977fe', NULL, '2026-06-22 21:20:54.245715', 'Health Ping', 'Job executed successfully', '2026-06-22 21:20:54.240714', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('cdc8201f-7da6-454e-a989-3f2363171a48', NULL, '2026-06-22 21:25:54.23719', 'Health Ping', 'Job executed successfully', '2026-06-22 21:25:54.233178', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('6da4058d-b1bc-4fab-af67-fe59d57ada7b', NULL, '2026-06-22 21:25:55.086644', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 21:25:55.07864', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('38f1b9a4-0ddd-4a0e-8975-06d8828e880e', NULL, '2026-06-22 21:30:54.275079', 'Health Ping', 'Job executed successfully', '2026-06-22 21:30:54.234752', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('99cd7978-3c5e-48c3-821d-2bb6081d2470', NULL, '2026-06-22 22:06:11.676366', 'Health Ping', 'Job executed successfully', '2026-06-22 22:06:11.615617', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('f5f4d8d0-3438-4069-957c-d8f7d7486a2b', NULL, '2026-06-22 22:06:13.604299', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 22:06:12.027347', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('8a386883-54d2-4694-93a1-81d14659e5b4', NULL, '2026-06-22 22:11:11.571287', 'Health Ping', 'Job executed successfully', '2026-06-22 22:11:11.566754', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('c163a7f9-c0b2-4974-8e97-ca7b5c846f68', NULL, '2026-06-22 22:16:11.566658', 'Health Ping', 'Job executed successfully', '2026-06-22 22:16:11.562655', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('a014657e-9134-45c6-964b-fc59edbe9a14', NULL, '2026-06-22 22:16:13.680865', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 22:16:13.675858', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('a8c343e7-7abd-4ef7-8ef3-772cbed085d3', NULL, '2026-06-22 22:21:11.571627', 'Health Ping', 'Job executed successfully', '2026-06-22 22:21:11.568618', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('ffb86908-e218-4686-98bd-fc2f64ffee8d', NULL, '2026-06-22 22:26:11.573604', 'Health Ping', 'Job executed successfully', '2026-06-22 22:26:11.569597', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('cafefa61-111c-4799-8439-14fb204fcd98', NULL, '2026-06-22 22:26:13.693612', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 22:26:13.689648', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('0691916a-4b9e-4be6-994d-ab9d5019b42e', NULL, '2026-06-22 22:31:11.605633', 'Health Ping', 'Job executed successfully', '2026-06-22 22:31:11.567301', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('7e6d1dc1-6e5f-4e0b-afa3-c0429a240ddf', NULL, '2026-06-22 22:36:11.573323', 'Health Ping', 'Job executed successfully', '2026-06-22 22:36:11.56859', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('41552495-a1f4-4d26-bd47-5083d5946b56', NULL, '2026-06-22 22:36:13.716812', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 22:36:13.705769', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('20c585de-3c5b-4253-9a42-625f3250558c', NULL, '2026-06-22 22:41:11.577759', 'Health Ping', 'Job executed successfully', '2026-06-22 22:41:11.572', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('2225d75d-f96e-4bed-9cdb-c99e271d0254', NULL, '2026-06-22 22:46:11.587946', 'Health Ping', 'Job executed successfully', '2026-06-22 22:46:11.568716', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('4c9db5f6-bea3-4202-bc55-84219b33a3f4', NULL, '2026-06-22 22:46:13.742525', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 22:46:13.735523', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('e5ce57de-3158-4ed2-adaa-cb7ce242884e', NULL, '2026-06-22 22:51:11.577431', 'Health Ping', 'Job executed successfully', '2026-06-22 22:51:11.560508', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('033eaa01-4a34-410f-b5a9-d414f8c58a82', NULL, '2026-06-22 22:56:11.570685', 'Health Ping', 'Job executed successfully', '2026-06-22 22:56:11.568096', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('1907e09f-486d-4052-9d44-7bf46091a110', NULL, '2026-06-22 22:56:13.766549', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 22:56:13.760776', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('8bc8a8e6-a58d-4db5-8736-5c35d5d9e85d', NULL, '2026-06-22 23:03:37.561088', 'Health Ping', 'Job executed successfully', '2026-06-22 23:03:37.558009', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('5a364540-c9c2-4d85-b490-b39411761a20', NULL, '2026-06-22 23:03:38.144993', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 23:03:37.774326', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('f364eb9a-7f42-4e05-88b5-1afc60dfd2ce', NULL, '2026-06-22 23:08:06.882898', 'Health Ping', 'Job executed successfully', '2026-06-22 23:08:06.87287', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('cb2979dd-01b9-4419-bd4a-229ec66c3113', NULL, '2026-06-22 23:08:07.640887', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 23:08:07.130277', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('5b84641b-788c-4536-967b-14ee18251696', NULL, '2026-06-22 23:13:06.795698', 'Health Ping', 'Job executed successfully', '2026-06-22 23:13:06.78343', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('39fdc1f7-89bf-42b3-88e1-aa99eede09f9', NULL, '2026-06-22 23:18:06.784905', 'Health Ping', 'Job executed successfully', '2026-06-22 23:18:06.775193', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('cb9d3482-9e85-48d6-8561-d90f7a74f4a6', NULL, '2026-06-22 23:18:07.672159', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 23:18:07.666161', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('243ecbf7-7891-46dc-9d04-4d49db236c85', NULL, '2026-06-22 23:23:06.937305', 'Health Ping', 'Job executed successfully', '2026-06-22 23:23:06.782175', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('e0d23d06-e341-4a37-a7d8-dec7474124ba', NULL, '2026-06-22 23:28:06.788866', 'Health Ping', 'Job executed successfully', '2026-06-22 23:28:06.783867', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('9f7f6c11-1308-4007-b75d-2716405ca562', NULL, '2026-06-22 23:28:07.692473', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 23:28:07.688466', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('6dfce756-57e6-40ea-8bf7-8944bd2ede35', NULL, '2026-06-22 23:33:06.79007', 'Health Ping', 'Job executed successfully', '2026-06-22 23:33:06.787069', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('a44dae4e-95a7-4da8-904e-48c7322aa18a', NULL, '2026-06-22 23:38:06.790596', 'Health Ping', 'Job executed successfully', '2026-06-22 23:38:06.787602', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('0acf1355-a0e4-4751-ac1a-b1449e4d2db5', NULL, '2026-06-22 23:38:07.719528', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 23:38:07.713636', 'SUCCESS', 'FIXED_DELAY');
INSERT INTO public.scheduled_job_logs VALUES ('2e80d595-637e-4d67-87e8-bb4cc90ee800', NULL, '2026-06-22 23:43:06.780686', 'Health Ping', 'Job executed successfully', '2026-06-22 23:43:06.777686', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('b81a6cdc-ef30-4aa9-955d-49ae7110fc09', NULL, '2026-06-22 23:48:06.798385', 'Health Ping', 'Job executed successfully', '2026-06-22 23:48:06.788363', 'SUCCESS', 'FIXED_RATE');
INSERT INTO public.scheduled_job_logs VALUES ('599d8340-b52c-4c66-affb-4e728e084d28', NULL, '2026-06-22 23:48:07.792297', 'Retry Failed Email', 'tidak ada notifikasi retry due', '2026-06-22 23:48:07.745206', 'SUCCESS', 'FIXED_DELAY');


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


-- Completed on 2026-06-22 23:50:29

--
-- PostgreSQL database dump complete
--

\unrestrict shEHp4ERK7OEIAEJn0uQAk2AgRxT3U8I1Wi5SgQ6M3s6fBVlFmbmdKqkbipn373


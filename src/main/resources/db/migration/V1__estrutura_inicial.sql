create table clientes (
    id bigserial primary key,
    nome varchar(150) not null,
    cpf varchar(11) not null unique,
    email varchar(254) not null,
    status varchar(20) not null,
    criado_em timestamp with time zone not null,
    atualizado_em timestamp with time zone not null,
    versao bigint not null default 0,
    constraint ck_clientes_status check (status in ('ACTIVE', 'INACTIVE'))
);

create index idx_clientes_nome on clientes (lower(nome));
create index idx_clientes_status on clientes (status);

create table outbox_eventos (
    id uuid primary key,
    tipo_evento varchar(80) not null,
    payload text not null,
    criado_em timestamp with time zone not null,
    publicado_em timestamp with time zone
);

create index idx_outbox_pendentes on outbox_eventos (criado_em) where publicado_em is null;

create table mensagens_processadas (
    evento_id uuid primary key,
    processado_em timestamp with time zone not null
);

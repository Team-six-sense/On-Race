# 1. VPC 생성
resource "aws_vpc" "this" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "${var.project_name}-${var.environment}-vpc"
  }
}

# 2. Public Subnets (ALB, NAT Gateway, Istio Ingress Proxy 용)
resource "aws_subnet" "public" {
  count                   = length(var.public_subnets)
  vpc_id                  = aws_vpc.this.id
  cidr_block              = var.public_subnets[count.index]
  availability_zone       = var.azs[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name                     = "${var.project_name}-${var.environment}-public-${var.azs[count.index]}"
    "kubernetes.io/role/elb" = "1" # [필수] EKS/Istio 외부 로드밸런서 자동 할당 태그
  }
}

# 3. Private Subnets (EKS Worker Nodes, SQS 연동 파드 용)
resource "aws_subnet" "private" {
  count             = length(var.private_subnets)
  vpc_id            = aws_vpc.this.id
  cidr_block        = var.private_subnets[count.index]
  availability_zone = var.azs[count.index]

  tags = {
    Name                              = "${var.project_name}-${var.environment}-private-${var.azs[count.index]}"
    "kubernetes.io/role/internal-elb" = "1" # [필수] EKS 내부 로드밸런서 자동 할당 태그

    # Karpenter가 서브넷을 찾기 위한 필수 식별 태그
    "karpenter.sh/discovery"          = "t6-on-race-prod-cluster"
  } 
}

# 4. Database Subnets (Redis, RDS 용)
resource "aws_subnet" "database" {
  count             = length(var.database_subnets)
  vpc_id            = aws_vpc.this.id
  cidr_block        = var.database_subnets[count.index]
  availability_zone = var.azs[count.index]

  tags = {
    Name = "${var.project_name}-${var.environment}-db-${var.azs[count.index]}"
  }
}

# 5. Internet Gateway
resource "aws_internet_gateway" "this" {
  vpc_id = aws_vpc.this.id

  tags = {
    Name = "${var.project_name}-${var.environment}-igw"
  }
}

# 6. NAT Gateway 및 EIP (single_nat_gateway 변수 제어)
resource "aws_eip" "nat" {
  count  = var.single_nat_gateway ? 1 : length(var.azs)
  domain = "vpc"
}

resource "aws_nat_gateway" "this" {
  count         = var.single_nat_gateway ? 1 : length(var.azs)
  allocation_id = aws_eip.nat[count.index].id
  subnet_id     = aws_subnet.public[count.index].id

  tags = {
    Name = "${var.project_name}-${var.environment}-nat-${count.index + 1}"
  }
  depends_on = [aws_internet_gateway.this]
}

# 7. Public Route Table & Association (IGW 연결)
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.this.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.this.id
  }

  tags = { Name = "${var.project_name}-${var.environment}-rt-public" }
}

resource "aws_route_table_association" "public" {
  count          = length(var.public_subnets)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

# 8. Private Route Table & Association (NAT Gateway 연동)
resource "aws_route_table" "private" {
  count  = length(var.private_subnets)
  vpc_id = aws_vpc.this.id

  route {
    cidr_block     = "0.0.0.0/0"
    # single_nat_gateway 분기 처리에 따른 NAT 매핑
    nat_gateway_id = var.single_nat_gateway ? aws_nat_gateway.this[0].id : aws_nat_gateway.this[count.index].id
  }

  tags = { Name = "${var.project_name}-${var.environment}-rt-private-${var.azs[count.index]}" }
}

resource "aws_route_table_association" "private" {
  count          = length(var.private_subnets)
  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private[count.index].id
}

# 9. Database Route Table & Association (인터넷 차단, Local 통신만 허용)
resource "aws_route_table" "database" {
  vpc_id = aws_vpc.this.id
  tags = { Name = "${var.project_name}-${var.environment}-rt-database" }
}

resource "aws_route_table_association" "database" {
  count          = length(var.database_subnets)
  subnet_id      = aws_subnet.database[count.index].id
  route_table_id = aws_route_table.database.id
}

# 10. v-main.tf 하단에 추가
resource "aws_vpc_endpoint" "sqs" {
  vpc_id              = aws_vpc.this.id
  service_name        = "com.amazonaws.${data.aws_region.current.name}.sqs"
  vpc_endpoint_type   = "Interface"
  subnet_ids          = aws_subnet.private[*].id
  security_group_ids  = [aws_security_group.vpc_endpoint.id]
  private_dns_enabled = true
}

# 리전 정보를 가져오기 위한 데이터 소스
data "aws_region" "current" {}

# VPC Endpoint 전용 보안 그룹 (EKS 노드에서 443 포트 접근 허용)
resource "aws_security_group" "vpc_endpoint" {
  name        = "${var.project_name}-${var.environment}-vpce-sg"
  description = "Security group for VPC Endpoints"
  vpc_id      = aws_vpc.this.id

  ingress {
    from_port       = 443
    to_port         = 443
    protocol        = "tcp"
    # EKS 노드 보안 그룹을 직접 참조하거나 프라이빗 서브넷 대역을 허용합니다.
    cidr_blocks     = [var.vpc_cidr] 
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-vpce-sg" }
}
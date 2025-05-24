using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace GoldBazaar.Shared.Models;

public record PaginationMetadata(int PageNumber, int PageSize, int TotalCount, int TotalPages);
